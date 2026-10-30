package com.smartcampus.service;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.enums.NotificationType;
import com.smartcampus.model.AvailabilityResult;
import com.smartcampus.model.BookingRequest;
import com.smartcampus.model.BookingRequestForm;
import com.smartcampus.model.BookingRequestItem;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.Resource;
import com.smartcampus.model.ResourceNeed;
import com.smartcampus.repository.BookingRepository;
import com.smartcampus.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final BookingAvailabilityService availabilityService;
    private final EquipmentAllocationService equipmentService;
    private final PortalNotificationService notificationService;

    public BookingService(BookingRepository bookingRepository, ResourceRepository resourceRepository,
                          BookingAvailabilityService availabilityService, EquipmentAllocationService equipmentService,
                          PortalNotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.availabilityService = availabilityService;
        this.equipmentService = equipmentService;
        this.notificationService = notificationService;
    }

    @Transactional
    public BookingRequest submit(BookingRequestForm form, CampusUser requester) {
        if (requester.getRole() == UserRole.ADMIN) throw new IllegalArgumentException("Administrators do not submit member booking requests.");
        if (requester.getRole() != UserRole.STUDENT && requester.getRole() != UserRole.CLUB_REPRESENTATIVE)
            throw new SecurityException("Only students and club representatives can request resources.");
        if (form.getEventDate() == null || form.getStartTime() == null || form.getEndTime() == null
                || LocalDateTime.of(form.getEventDate(), form.getStartTime()).isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Choose an event time that has not passed.");
        }
        if (form.getExpectedParticipants() < 1 || form.getExpectedParticipants() > 10000)
            throw new IllegalArgumentException("Expected participants must be between 1 and 10000.");
        List<ResourceNeed> needs = resolveNeeds(form);
        bookingRepository.lockResources(form.getResourceIds());
        AvailabilityResult availability = availabilityService.check(form, needs, null);
        if (!availability.available()) throw new BookingUnavailableException(availability);
        long id = bookingRepository.insertRequest(requester, form);
        bookingRepository.insertItems(id, form, needs.stream().map(ResourceNeed::resource).toList());
        long historyId = bookingRepository.addHistory(id, BookingStatus.PENDING, "REQUEST_SUBMITTED", "Request submitted for administrator review.", requester.getUserId());
        notificationService.bookingSubmitted(id, requester.getUserId(), form.getEventTitle().trim(), historyId);
        return bookingRepository.findById(id).orElseThrow();
    }

    @Transactional
    public BookingRequest transition(long bookingId, BookingStatus next, CampusUser admin, String note,
                                     LocalDate alternativeDate, LocalTime alternativeStart, LocalTime alternativeEnd) {
        requireAdmin(admin);
        BookingRequest current = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException());
        List<BookingRequestItem> items = bookingRepository.findItems(bookingId);
        bookingRepository.lockResources(items.stream().map(BookingRequestItem::resourceId).toList());
        if (!isAllowed(current.status(), next)) throw new InvalidBookingTransitionException(current.status(), next);
        if (note != null && note.length() > 1200) throw new IllegalArgumentException("Decision notes must be 1200 characters or fewer.");
        if (next == BookingStatus.REJECTED && blank(note)) throw new IllegalArgumentException("A reason is required when rejecting a request.");
        if (next == BookingStatus.ADDITIONAL_INFORMATION_REQUIRED && blank(note)) throw new IllegalArgumentException("Explain what information is needed.");
        if (next == BookingStatus.ALTERNATIVE_PROPOSED) {
            if (alternativeDate == null || alternativeStart == null || alternativeEnd == null
                    || !alternativeEnd.isAfter(alternativeStart) || alternativeDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Provide a valid alternative date and time.");
            }
            if (LocalDateTime.of(alternativeDate, alternativeStart).isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("The proposed alternative time must not have passed.");
            }
            AvailabilityResult alternative = availabilityService.check(alternativeDate, alternativeStart, alternativeEnd,
                    current.expectedParticipants(), resolveItemNeeds(items), current.bookingId());
            if (!alternative.available()) throw new BookingUnavailableException(alternative);
        }
        if (next == BookingStatus.APPROVED) {
            AvailabilityResult availability = availabilityService.check(current, items, current.bookingId());
            if (!availability.available()) throw new BookingUnavailableException(availability);
        }
        if (next == BookingStatus.COMPLETED) {
            if (!equipmentService.allReturned(bookingId)) {
                throw new IllegalArgumentException("Receive all issued equipment before completing this booking.");
            }
            if (LocalDateTime.of(current.eventDate(), current.endTime()).isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("A request can be completed after its scheduled end time.");
            }
        }
        if (!bookingRepository.changeStatus(bookingId, current.status(), next, note,
                next == BookingStatus.ALTERNATIVE_PROPOSED ? alternativeDate : null,
                next == BookingStatus.ALTERNATIVE_PROPOSED ? alternativeStart : null,
                next == BookingStatus.ALTERNATIVE_PROPOSED ? alternativeEnd : null)) {
            throw new InvalidBookingTransitionException(current.status(), next);
        }
        long historyId = bookingRepository.addHistory(bookingId, next, next.name(), note, admin.getUserId());
        BookingRequest updated = bookingRepository.findById(bookingId).orElseThrow();
        NotificationType type = switch (next) {
            case APPROVED -> NotificationType.BOOKING_APPROVED;
            case REJECTED -> NotificationType.BOOKING_REJECTED;
            case ADDITIONAL_INFORMATION_REQUIRED -> NotificationType.INFORMATION_REQUESTED;
            case ALTERNATIVE_PROPOSED -> NotificationType.ALTERNATIVE_PROPOSED;
            default -> null;
        };
        if (type != null) {
            String title = switch (type) {
                case BOOKING_APPROVED -> "Booking approved";
                case BOOKING_REJECTED -> "Booking rejected";
                case INFORMATION_REQUESTED -> "Additional information requested";
                case ALTERNATIVE_PROPOSED -> "Alternative time proposed";
                default -> "Booking update";
            };
            String message = note == null || note.isBlank() ? "Your booking request “" + updated.eventTitle() + "” has been updated." : note;
            notificationService.bookingStatus(bookingId, updated.requesterId(), type, title, message, historyId);
        }
        if (next == BookingStatus.APPROVED) equipmentService.reserveApprovedRequest(updated, items);
        if (next == BookingStatus.REJECTED || next == BookingStatus.CANCELLED) equipmentService.releaseReservations(bookingId);
        if (next == BookingStatus.COMPLETED) bookingRepository.recordCompletedUsage(updated, items);
        return updated;
    }

    @Transactional
    public BookingRequest cancelByRequester(long bookingId, CampusUser requester) {
        BookingRequest current = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);
        if (current.requesterId() != requester.getUserId()) throw new SecurityException("You can only cancel your own request.");
        if (current.status() != BookingStatus.PENDING && current.status() != BookingStatus.UNDER_REVIEW
                && current.status() != BookingStatus.ADDITIONAL_INFORMATION_REQUIRED
                && current.status() != BookingStatus.ALTERNATIVE_PROPOSED
                && !(current.status() == BookingStatus.APPROVED && LocalDateTime.of(current.eventDate(), current.startTime()).isAfter(LocalDateTime.now()))) {
            throw new InvalidBookingTransitionException(current.status(), BookingStatus.CANCELLED);
        }
        if (!bookingRepository.changeStatus(bookingId, current.status(), BookingStatus.CANCELLED,
                "Cancelled by requester.", null, null, null)) throw new InvalidBookingTransitionException(current.status(), BookingStatus.CANCELLED);
        bookingRepository.addHistory(bookingId, BookingStatus.CANCELLED, "CANCELLED_BY_REQUESTER", null, requester.getUserId());
        equipmentService.releaseReservations(bookingId);
        return bookingRepository.findById(bookingId).orElseThrow();
    }

    @Transactional
    public BookingRequest provideAdditionalInformation(long bookingId, CampusUser requester, String information) {
        BookingRequest current = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);
        if (current.requesterId() != requester.getUserId()) throw new SecurityException("You can only update your own request.");
        if (information == null || information.isBlank() || information.length() > 1200)
            throw new IllegalArgumentException("Enter additional information (up to 1200 characters).");
        if (!bookingRepository.addRequesterInformation(bookingId, information))
            throw new InvalidBookingTransitionException(current.status(), BookingStatus.UNDER_REVIEW);
        bookingRepository.addHistory(bookingId, BookingStatus.UNDER_REVIEW, "INFORMATION_PROVIDED", information, requester.getUserId());
        return bookingRepository.findById(bookingId).orElseThrow();
    }

    @Transactional
    public BookingRequest acceptAlternative(long bookingId, CampusUser requester) {
        BookingRequest current = bookingRepository.findById(bookingId).orElseThrow(BookingNotFoundException::new);
        if (current.requesterId() != requester.getUserId()) throw new SecurityException("You can only respond to your own request.");
        if (current.status() != BookingStatus.ALTERNATIVE_PROPOSED || current.alternativeDate() == null)
            throw new InvalidBookingTransitionException(current.status(), BookingStatus.UNDER_REVIEW);
        List<BookingRequestItem> items = bookingRepository.findItems(bookingId);
        bookingRepository.lockResources(items.stream().map(BookingRequestItem::resourceId).toList());
        AvailabilityResult availability = availabilityService.check(current.alternativeDate(), current.alternativeStartTime(),
                current.alternativeEndTime(), current.expectedParticipants(), resolveItemNeeds(items), bookingId);
        if (!availability.available()) throw new BookingUnavailableException(availability);
        if (!bookingRepository.acceptAlternative(bookingId)) throw new InvalidBookingTransitionException(current.status(), BookingStatus.UNDER_REVIEW);
        bookingRepository.addHistory(bookingId, BookingStatus.UNDER_REVIEW, "ALTERNATIVE_ACCEPTED", "Requester accepted the proposed slot.", requester.getUserId());
        return bookingRepository.findById(bookingId).orElseThrow();
    }

    public List<BookingRequest> forRequester(long userId) { return bookingRepository.findForRequester(userId); }
    public List<BookingRequest> searchAll(String search, BookingStatus status, com.smartcampus.enums.ResourceCategory category,
                                         LocalDate date) { return bookingRepository.searchAll(search, status, category, date); }
    public List<BookingStatus> allowedNextStatuses(BookingStatus current) {
        return java.util.Arrays.stream(BookingStatus.values()).filter(next -> isAllowed(current, next)).toList();
    }
    public java.util.Optional<BookingRequest> find(long id) { return bookingRepository.findById(id); }
    public List<BookingRequestItem> items(long id) { return bookingRepository.findItems(id); }
    public List<com.smartcampus.model.BookingHistoryEntry> history(long id) { return bookingRepository.findHistory(id); }
    public List<Resource> requestableResources() { return resourceRepository.findAll(); }
    public java.util.Optional<Resource> findResource(long id) { return resourceRepository.findById(id); }

    public List<ResourceNeed> resolveNeeds(BookingRequestForm form) {
        if (form.getResourceIds() == null || form.getRequestedQuantities() == null
                || form.getResourceIds().isEmpty() || form.getResourceIds().size() != form.getRequestedQuantities().size())
            throw new IllegalArgumentException("Choose a resource and quantity for every requested item.");
        if (form.getResourceIds().size() > 10) throw new IllegalArgumentException("Choose up to 10 resources per request.");
        if (new HashSet<>(form.getResourceIds()).size() != form.getResourceIds().size())
            throw new IllegalArgumentException("Select each resource only once.");
        List<ResourceNeed> needs = new ArrayList<>();
        for (int index = 0; index < form.getResourceIds().size(); index++) {
            Integer quantity = form.getRequestedQuantities().get(index);
            if (quantity == null || quantity < 1) throw new IllegalArgumentException("Each requested quantity must be at least one.");
            Resource resource = resourceRepository.findById(form.getResourceIds().get(index))
                    .orElseThrow(() -> new IllegalArgumentException("One selected resource no longer exists."));
            needs.add(new ResourceNeed(resource, quantity));
        }
        return List.copyOf(needs);
    }

    private List<ResourceNeed> resolveItemNeeds(List<BookingRequestItem> items) {
        List<ResourceNeed> needs = new ArrayList<>();
        for (BookingRequestItem item : items) {
            Resource resource = resourceRepository.findById(item.resourceId()).orElseThrow();
            needs.add(new ResourceNeed(resource, item.requestedQuantity()));
        }
        return needs;
    }

    public List<ResourceNeed> resolveNeeds(List<BookingRequestItem> items) { return resolveItemNeeds(items); }

    private static boolean isAllowed(BookingStatus current, BookingStatus next) {
        return switch (next) {
            case UNDER_REVIEW -> current == BookingStatus.PENDING || current == BookingStatus.ADDITIONAL_INFORMATION_REQUIRED;
            case ADDITIONAL_INFORMATION_REQUIRED, ALTERNATIVE_PROPOSED, REJECTED ->
                    current == BookingStatus.PENDING || current == BookingStatus.UNDER_REVIEW
                            || current == BookingStatus.ADDITIONAL_INFORMATION_REQUIRED || current == BookingStatus.ALTERNATIVE_PROPOSED;
            case APPROVED -> current == BookingStatus.PENDING || current == BookingStatus.UNDER_REVIEW
                    || current == BookingStatus.ADDITIONAL_INFORMATION_REQUIRED;
            case CANCELLED -> current == BookingStatus.APPROVED;
            case COMPLETED -> current == BookingStatus.APPROVED;
            case PENDING -> false;
        };
    }

    private static boolean blank(String value) { return value == null || value.isBlank(); }
    private static void requireAdmin(CampusUser admin) {
        if (admin == null || admin.getRole() != UserRole.ADMIN) throw new SecurityException("Only administrators may review booking requests.");
    }

    public static class BookingUnavailableException extends RuntimeException {
        private final AvailabilityResult availability;
        public BookingUnavailableException(AvailabilityResult availability) {
            super(String.join(" ", availability.issues())); this.availability = availability;
        }
        public AvailabilityResult getAvailability() { return availability; }
    }
    public static class BookingNotFoundException extends RuntimeException { }
    public static class InvalidBookingTransitionException extends RuntimeException {
        public InvalidBookingTransitionException(BookingStatus from, BookingStatus to) {
            super("Booking cannot move from " + from + " to " + to + ".");
        }
    }
}
