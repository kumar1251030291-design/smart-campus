package com.smartcampus.controller;

import com.smartcampus.enums.EventType;
import com.smartcampus.enums.BookingStatus;
import com.smartcampus.model.BookingAvailabilityFeedback;
import com.smartcampus.model.BookingRequestItem;
import com.smartcampus.model.BookingRequestForm;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.ResourceNeed;
import com.smartcampus.model.SlotSuggestion;
import com.smartcampus.service.BookingAvailabilityService;
import com.smartcampus.service.BookingService;
import com.smartcampus.service.MultiResourceBundlingService;
import com.smartcampus.service.PredictiveSlotSuggestionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
public class BookingController {
    private final BookingService bookingService;
    private final BookingAvailabilityService availabilityService;
    private final MultiResourceBundlingService bundlingService;
    private final PredictiveSlotSuggestionService predictiveService;
    private final com.smartcampus.service.EquipmentAllocationService equipmentService;

    public BookingController(BookingService bookingService, BookingAvailabilityService availabilityService,
                             MultiResourceBundlingService bundlingService, PredictiveSlotSuggestionService predictiveService,
                             com.smartcampus.service.EquipmentAllocationService equipmentService) {
        this.bookingService = bookingService; this.availabilityService = availabilityService;
        this.bundlingService = bundlingService; this.predictiveService = predictiveService;
        this.equipmentService = equipmentService;
    }

    @GetMapping({"/student/bookings", "/club/bookings"})
    public String myBookings(@RequestParam(required = false) BookingStatus status,
                             @RequestParam(defaultValue = "") String filter,
                             @AuthenticationPrincipal CampusUser user, Model model) {
        List<com.smartcampus.model.BookingRequest> requests = bookingService.forRequester(user.getUserId());
        if (status != null) requests = requests.stream().filter(request -> request.status() == status).toList();
        if ("upcoming".equalsIgnoreCase(filter)) requests = requests.stream()
                .filter(request -> request.status() == BookingStatus.APPROVED)
                .filter(request -> java.time.LocalDateTime.of(request.eventDate(), request.startTime()).isAfter(java.time.LocalDateTime.now())).toList();
        if ("completed".equalsIgnoreCase(filter)) requests = requests.stream().filter(r -> r.status() == BookingStatus.COMPLETED).toList();
        model.addAttribute("requests", requests);
        Map<Long, List<BookingRequestItem>> itemsByBooking = new LinkedHashMap<>();
        requests.forEach(request -> itemsByBooking.put(request.bookingId(), bookingService.items(request.bookingId())));
        model.addAttribute("itemsByBooking", itemsByBooking);
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("selectedStatus", status == null ? "" : status.name());
        model.addAttribute("filter", filter);
        model.addAttribute("memberPath", memberPath(user));
        return "bookings/my-requests";
    }

    @GetMapping({"/student/bookings/new", "/club/bookings/new"})
    public String newBooking(@RequestParam(required = false) Long resourceId,
                             @AuthenticationPrincipal CampusUser user, Model model) {
        BookingRequestForm form = new BookingRequestForm();
        if (resourceId != null && bookingService.findResource(resourceId).isPresent()) {
            form.setResourceIds(List.of(resourceId));
            form.setRequestedQuantities(List.of(1));
        }
        prepareForm(model, form, memberPath(user), user);
        return "bookings/request-form";
    }

    @PostMapping({"/student/bookings", "/club/bookings"})
    public String submit(@Valid @ModelAttribute("bookingForm") BookingRequestForm form, BindingResult errors,
                         @AuthenticationPrincipal CampusUser user, Model model) {
        validateTimes(form, errors);
        if (errors.hasErrors()) { prepareForm(model, form, memberPath(user), user); return "bookings/request-form"; }
        try {
            var request = bookingService.submit(form, user);
            return "redirect:/" + (user.getRole().name().equals("STUDENT") ? "student" : "club")
                    + "/bookings/" + request.bookingId() + "?submitted";
        } catch (BookingService.BookingUnavailableException | IllegalArgumentException exception) {
            model.addAttribute("submitError", exception.getMessage());
            prepareForm(model, form, memberPath(user), user);
            return "bookings/request-form";
        }
    }

    @GetMapping({"/student/bookings/{id}", "/club/bookings/{id}"})
    public String details(@PathVariable long id, @AuthenticationPrincipal CampusUser user, Model model) {
        var request = bookingService.find(id).filter(item -> item.requesterId() == user.getUserId());
        if (request.isEmpty()) return "redirect:/" + memberPath(user) + "/bookings?error=not-found";
        model.addAttribute("request", request.get());
        model.addAttribute("eventDateDisplay", request.get().eventDate().format(DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH)));
        model.addAttribute("startTimeDisplay", formatTime(request.get().startTime()));
        model.addAttribute("endTimeDisplay", formatTime(request.get().endTime()));
        model.addAttribute("durationDisplay", formatDuration(Duration.between(request.get().startTime(), request.get().endTime())));
        model.addAttribute("submittedDisplay", request.get().createdAt().format(DateTimeFormatter.ofPattern("dd MMM uuuu, hh:mm a", Locale.ENGLISH)));
        model.addAttribute("campusUser", user);
        model.addAttribute("items", bookingService.items(id));
        model.addAttribute("history", bookingService.history(id));
        model.addAttribute("allocations", equipmentService.forBooking(id));
        model.addAttribute("memberPath", memberPath(user));
        BookingStatus currentStatus = request.get().status();
        model.addAttribute("canCancel", currentStatus == BookingStatus.PENDING || currentStatus == BookingStatus.UNDER_REVIEW
                || currentStatus == BookingStatus.ADDITIONAL_INFORMATION_REQUIRED || currentStatus == BookingStatus.ALTERNATIVE_PROPOSED
                || (currentStatus == BookingStatus.APPROVED && java.time.LocalDateTime.of(request.get().eventDate(), request.get().startTime()).isAfter(java.time.LocalDateTime.now())));
        return "bookings/request-details";
    }

    private String formatTime(LocalTime time) {
        return time.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH));
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        String hourLabel = hours == 1 ? "hour" : "hours";
        if (minutes == 0) return hours + " " + hourLabel;
        String minuteLabel = minutes == 1 ? "minute" : "minutes";
        return hours == 0 ? minutes + " " + minuteLabel : hours + " " + hourLabel + " " + minutes + " " + minuteLabel;
    }

    @GetMapping({"/student/bookings/availability", "/club/bookings/availability"})
    @ResponseBody
    public BookingAvailabilityFeedback checkAvailability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam int expectedParticipants, @RequestParam List<Long> resourceIds,
            @RequestParam List<Integer> requestedQuantities) {
        try {
            BookingRequestForm form = new BookingRequestForm();
            form.setEventDate(eventDate); form.setStartTime(startTime); form.setEndTime(endTime);
            form.setExpectedParticipants(expectedParticipants); form.setResourceIds(resourceIds);
            form.setRequestedQuantities(requestedQuantities);
            if (LocalDateTime.of(eventDate, startTime).isBefore(LocalDateTime.now()))
                return unavailable("Choose an event time that has not passed.");
            if (!endTime.isAfter(startTime)) return unavailable("End time must be later than start time.");
            List<ResourceNeed> needs = bookingService.resolveNeeds(form);
            if (expectedParticipants < 1 || expectedParticipants > 10000)
                return unavailable("Expected participants must be between 1 and 10000.");
            var result = availabilityService.check(form, needs, null);
            if (result.available()) return new BookingAvailabilityFeedback(true, List.of(), List.of(), List.of());
            List<SlotSuggestion> alternatives = bundlingService.findAlternatives(eventDate, startTime, endTime,
                    expectedParticipants, needs, null);
            int durationHours = Math.max(1, (int) Math.ceil(Duration.between(startTime, endTime).toMinutes() / 60.0));
            List<SlotSuggestion> predictive = new ArrayList<>();
            for (ResourceNeed need : needs) predictive.addAll(predictiveService.suggest(need.resource().getId(),
                    need.quantity(), expectedParticipants, durationHours));
            predictive = predictive.stream().collect(java.util.stream.Collectors.toMap(
                            suggestion -> suggestion.date() + "-" + suggestion.startTime(), suggestion -> suggestion,
                            (first, second) -> first.score() >= second.score() ? first : second, LinkedHashMap::new))
                    .values().stream().sorted(java.util.Comparator.comparingDouble(SlotSuggestion::score).reversed()).limit(3).toList();
            return new BookingAvailabilityFeedback(false, result.issues(), alternatives, predictive);
        } catch (IllegalArgumentException exception) {
            return unavailable(exception.getMessage());
        }
    }

    @PostMapping({"/student/bookings/{id}/cancel", "/club/bookings/{id}/cancel"})
    public String cancel(@PathVariable long id, @AuthenticationPrincipal CampusUser user) {
        try { bookingService.cancelByRequester(id, user); return "redirect:/" + memberPath(user) + "/bookings/" + id + "?updated"; }
        catch (RuntimeException ex) { return "redirect:/" + memberPath(user) + "/bookings/" + id + "?error=action"; }
    }

    @PostMapping({"/student/bookings/{id}/information", "/club/bookings/{id}/information"})
    public String information(@PathVariable long id, @RequestParam String information,
                              @AuthenticationPrincipal CampusUser user) {
        try { bookingService.provideAdditionalInformation(id, user, information); return "redirect:/" + memberPath(user) + "/bookings/" + id + "?updated"; }
        catch (RuntimeException ex) { return "redirect:/" + memberPath(user) + "/bookings/" + id + "?error=action"; }
    }

    @PostMapping({"/student/bookings/{id}/accept-alternative", "/club/bookings/{id}/accept-alternative"})
    public String acceptAlternative(@PathVariable long id, @AuthenticationPrincipal CampusUser user) {
        try { bookingService.acceptAlternative(id, user); return "redirect:/" + memberPath(user) + "/bookings/" + id + "?updated"; }
        catch (RuntimeException ex) { return "redirect:/" + memberPath(user) + "/bookings/" + id + "?error=action"; }
    }

    private void prepareForm(Model model, BookingRequestForm form, String memberPath, CampusUser user) {
        model.addAttribute("bookingForm", form);
        model.addAttribute("memberPath", memberPath);
        model.addAttribute("campusUser", user);
        model.addAttribute("resources", bookingService.requestableResources());
        model.addAttribute("eventTypes", EventType.values());
        model.addAttribute("minDate", LocalDate.now());
    }
    private BookingAvailabilityFeedback unavailable(String message) {
        return new BookingAvailabilityFeedback(false, List.of(message), List.of(), List.of());
    }
    private void validateTimes(BookingRequestForm form, BindingResult errors) {
        if (form.getStartTime() != null && form.getEndTime() != null && !form.getEndTime().isAfter(form.getStartTime()))
            errors.rejectValue("endTime", "booking.time", "End time must be later than start time.");
    }
    private String memberPath(CampusUser user) { return user.getRole().name().equals("STUDENT") ? "student" : "club"; }
}
