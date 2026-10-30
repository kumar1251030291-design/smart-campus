package com.smartcampus.service;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.model.AvailabilityResult;
import com.smartcampus.model.BookingRequest;
import com.smartcampus.model.BookingRequestForm;
import com.smartcampus.model.BookingRequestItem;
import com.smartcampus.model.ResourceNeed;
import com.smartcampus.repository.BookingRepository;
import com.smartcampus.repository.EquipmentRepository;
import com.smartcampus.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingAvailabilityService {
    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final EquipmentRepository equipmentRepository;

    public BookingAvailabilityService(BookingRepository bookingRepository, ResourceRepository resourceRepository,
                                      EquipmentRepository equipmentRepository) {
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.equipmentRepository = equipmentRepository;
    }

    public AvailabilityResult check(BookingRequestForm form, List<ResourceNeed> needs, Long excludeBookingId) {
        return check(form.getEventDate(), form.getStartTime(), form.getEndTime(),
                form.getExpectedParticipants(), needs, excludeBookingId);
    }

    public Optional<com.smartcampus.model.Resource> getResource(long id) { return resourceRepository.findById(id); }

    public AvailabilityResult check(BookingRequest booking, List<BookingRequestItem> items, Long excludeBookingId) {
        List<ResourceNeed> needs = new ArrayList<>();
        for (BookingRequestItem item : items) {
            var resource = resourceRepository.findById(item.resourceId()).orElse(null);
            if (resource == null) return new AvailabilityResult(false,
                    List.of("A resource in this request no longer exists."), List.of());
            needs.add(new ResourceNeed(resource, item.requestedQuantity()));
        }
        return check(booking.eventDate(), booking.startTime(), booking.endTime(), booking.expectedParticipants(), needs, excludeBookingId);
    }

    public AvailabilityResult check(LocalDate date, LocalTime start, LocalTime end, int participants,
                                    List<ResourceNeed> needs, Long excludeBookingId) {
        List<String> issues = new ArrayList<>();
        Map<Long, BookingRequest> conflicts = new LinkedHashMap<>();
        if (date == null || start == null || end == null || !end.isAfter(start)) {
            issues.add("The end time must be later than the start time.");
            return new AvailabilityResult(false, issues, List.of());
        }
        for (ResourceNeed need : needs) {
            var resource = need.resource();
            int requested = need.quantity();
            if (resource.getAvailabilityStatus() == AvailabilityStatus.UNAVAILABLE
                    || resource.getAvailabilityStatus() == AvailabilityStatus.MAINTENANCE) {
                issues.add(resource.getName() + " is marked unavailable or under maintenance.");
                continue;
            }
            if (requested < 1) {
                issues.add("Requested quantity for " + resource.getName() + " must be at least one.");
                continue;
            }
            if ((resource.getCategory() == ResourceCategory.CLASSROOM || resource.getCategory() == ResourceCategory.FACILITY
                    || resource.getCategory() == ResourceCategory.LABORATORY)
                    && participants > resource.getCapacityOrQuantity()) {
                issues.add(resource.getName() + " capacity is " + resource.getCapacityOrQuantity()
                        + "; expected participants are " + participants + ".");
            }
            if ((resource.getCategory() == ResourceCategory.EQUIPMENT || resource.getCategory() == ResourceCategory.LABORATORY)
                    && requested > resource.getCapacityOrQuantity()) {
                issues.add("Requested quantity for " + resource.getName() + " exceeds its capacity of "
                        + resource.getCapacityOrQuantity() + ".");
            }
            if ((resource.getCategory() == ResourceCategory.CLASSROOM || resource.getCategory() == ResourceCategory.FACILITY)
                    && requested != 1) {
                issues.add(resource.getName() + " is a venue and must use quantity 1.");
            }
            int usableCapacity = resource.getCapacityOrQuantity();
            if (resource.getCategory() == ResourceCategory.EQUIPMENT) {
                usableCapacity -= equipmentRepository.countUnavailableAssets(resource.getId());
            }
            List<BookingRequest> existing = bookingRepository.findConflicts(resource.getId(), date, start, end, excludeBookingId);
            existing.forEach(conflict -> conflicts.put(conflict.bookingId(), conflict));
            if (resource.getCategory() == ResourceCategory.EQUIPMENT || resource.getCategory() == ResourceCategory.LABORATORY) {
                int allocated = bookingRepository.sumOverlappingQuantity(resource.getId(), date, start, end, excludeBookingId);
                if (allocated + requested > usableCapacity) {
                    issues.add(resource.getName() + " has only " + Math.max(0, usableCapacity - allocated)
                            + " units/workstations left for this time slot.");
                }
                if (resource.getCategory() == ResourceCategory.LABORATORY) {
                    int people = bookingRepository.sumOverlappingParticipants(resource.getId(), date, start, end, excludeBookingId);
                    if (people + participants > resource.getCapacityOrQuantity()) {
                        issues.add("The participant capacity for " + resource.getName() + " is already allocated in this time slot.");
                    }
                }
            } else if (!existing.isEmpty()) {
                issues.add(resource.getName() + " already has an overlapping request for this date and time.");
            }
        }
        return new AvailabilityResult(issues.isEmpty(), List.copyOf(issues), List.copyOf(conflicts.values()));
    }
}
