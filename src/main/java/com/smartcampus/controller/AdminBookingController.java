package com.smartcampus.controller;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.EquipmentStatus;
import com.smartcampus.enums.ReturnCondition;
import com.smartcampus.model.CampusUser;
import com.smartcampus.service.BookingService;
import com.smartcampus.service.EquipmentAllocationService;
import com.smartcampus.service.MultiResourceBundlingService;
import com.smartcampus.service.PriorityWeightedDynamicSlotReallocationService;
import com.smartcampus.service.PredictiveSlotSuggestionService;
import com.smartcampus.service.BookingAvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Controller
public class AdminBookingController {
    private final BookingService bookings;
    private final EquipmentAllocationService equipment;
    private final BookingAvailabilityService availability;
    private final MultiResourceBundlingService bundling;
    private final PredictiveSlotSuggestionService predictive;
    private final PriorityWeightedDynamicSlotReallocationService priorities;

    public AdminBookingController(BookingService bookings, EquipmentAllocationService equipment,
            BookingAvailabilityService availability, MultiResourceBundlingService bundling,
            PredictiveSlotSuggestionService predictive, PriorityWeightedDynamicSlotReallocationService priorities) {
        this.bookings = bookings; this.equipment = equipment; this.availability = availability;
        this.bundling = bundling; this.predictive = predictive; this.priorities = priorities;
    }

    @GetMapping("/admin/bookings")
    public String list(@RequestParam(required = false) String search, @RequestParam(required = false) BookingStatus status,
                       @RequestParam(required = false) com.smartcampus.enums.ResourceCategory category,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date, Model model) {
        model.addAttribute("requests", bookings.searchAll(search, status, category, date));
        model.addAttribute("statuses", BookingStatus.values()); model.addAttribute("categories", com.smartcampus.enums.ResourceCategory.values());
        model.addAttribute("search", search == null ? "" : search); model.addAttribute("selectedStatus", status == null ? "" : status.name());
        model.addAttribute("selectedCategory", category == null ? "" : category.name()); model.addAttribute("date", date);
        return "admin/bookings";
    }

    @GetMapping("/admin/bookings/{id}")
    public String details(@PathVariable long id, Model model) {
        var request = bookings.find(id);
        if (request.isEmpty()) return "redirect:/admin/bookings?error=not-found";
        var booking = request.get(); var items = bookings.items(id);
        model.addAttribute("request", booking); model.addAttribute("items", items); model.addAttribute("history", bookings.history(id));
        model.addAttribute("eventDateDisplay", booking.eventDate().format(DateTimeFormatter.ofPattern("dd MMM uuuu", Locale.ENGLISH)));
        model.addAttribute("startTimeDisplay", formatTime(booking.startTime()));
        model.addAttribute("endTimeDisplay", formatTime(booking.endTime()));
        model.addAttribute("durationDisplay", formatDuration(Duration.between(booking.startTime(), booking.endTime())));
        model.addAttribute("allocations", equipment.forBooking(id)); model.addAttribute("statuses", bookings.allowedNextStatuses(booking.status()));
        model.addAttribute("returnConditions", ReturnCondition.values());
        model.addAttribute("suggestions", bundling.findAlternatives(booking.eventDate(), booking.startTime(), booking.endTime(),
                booking.expectedParticipants(), bookings.resolveNeeds(items), booking.bookingId()));
        model.addAttribute("predictiveSuggestions", items.isEmpty() ? java.util.List.of() : predictive.suggest(items.getFirst().resourceId(),
                items.getFirst().requestedQuantity(), booking.expectedParticipants(), Math.max(1,
                        (int) Math.ceil(java.time.Duration.between(booking.startTime(), booking.endTime()).toMinutes() / 60.0))));
        if (!items.isEmpty()) model.addAttribute("priority", priorities.calculate(booking, items.getFirst().resourceId()));
        model.addAttribute("availability", availability.check(booking, items, booking.bookingId()));
        return "admin/booking-details";
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

    @PostMapping("/admin/bookings/{id}/status")
    public String transition(@PathVariable long id, @RequestParam BookingStatus status,
            @RequestParam(required = false) String note,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate alternativeDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime alternativeStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime alternativeEnd,
            @AuthenticationPrincipal CampusUser admin, RedirectAttributes redirectAttributes) {
        try { bookings.transition(id, status, admin, note, alternativeDate, alternativeStart, alternativeEnd); return "redirect:/admin/bookings/" + id + "?updated"; }
        catch (RuntimeException exception) {
            String safeMessage = exception instanceof IllegalArgumentException
                    || exception instanceof BookingService.BookingUnavailableException
                    || exception instanceof BookingService.InvalidBookingTransitionException
                    ? exception.getMessage() : "The review action could not be completed. Please retry.";
            redirectAttributes.addFlashAttribute("errorMessage", safeMessage);
            return "redirect:/admin/bookings/" + id + "?error=action";
        }
    }

    @GetMapping("/admin/equipment")
    public String equipment(@RequestParam(required = false) String search,
                            @RequestParam(required = false) EquipmentStatus status, Model model) {
        model.addAttribute("allocations", equipment.search(search, status)); model.addAttribute("assets", equipment.assets());
        model.addAttribute("statuses", EquipmentStatus.values()); model.addAttribute("returnConditions", ReturnCondition.values());
        model.addAttribute("search", search == null ? "" : search); model.addAttribute("selectedStatus", status == null ? "" : status.name());
        model.addAttribute("defaultReturn", LocalDateTime.now().plusHours(4));
        return "admin/equipment";
    }

    @PostMapping("/admin/equipment/{id}/issue")
    public String issue(@PathVariable long id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expectedReturn,
                        @AuthenticationPrincipal CampusUser admin) {
        return "redirect:/admin/equipment?" + (equipment.issue(id, admin, expectedReturn) ? "updated" : "error=action");
    }
    @PostMapping("/admin/equipment/{id}/receive")
    public String receive(@PathVariable long id, @RequestParam ReturnCondition condition, @RequestParam(required=false) String note,
                          @AuthenticationPrincipal CampusUser admin) {
        return "redirect:/admin/equipment?" + (equipment.receive(id, admin, condition, note) ? "updated" : "error=action");
    }
}
