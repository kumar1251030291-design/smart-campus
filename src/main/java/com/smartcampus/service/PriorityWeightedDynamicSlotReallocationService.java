package com.smartcampus.service;

import com.smartcampus.enums.EventType;
import com.smartcampus.model.BookingRequest;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.FairnessSummary;
import com.smartcampus.model.PriorityResult;
import com.smartcampus.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/** Transparent priority scoring. Recommendations never move an approved request automatically. */
@Service
public class PriorityWeightedDynamicSlotReallocationService {
    private final FairUsageAutoBalancingService fairnessService;
    private final BookingRepository bookingRepository;
    private final double eventWeight, roleWeight, advanceWeight, usageWeight, fairnessWeight;

    public PriorityWeightedDynamicSlotReallocationService(FairUsageAutoBalancingService fairnessService,
            BookingRepository bookingRepository,
            @Value("${smartcampus.priority.event-weight:3.0}") double eventWeight,
            @Value("${smartcampus.priority.role-weight:2.0}") double roleWeight,
            @Value("${smartcampus.priority.advance-weight:0.1}") double advanceWeight,
            @Value("${smartcampus.priority.usage-weight:0.5}") double usageWeight,
            @Value("${smartcampus.priority.fairness-weight:0.05}") double fairnessWeight) {
        this.fairnessService = fairnessService;
        this.bookingRepository = bookingRepository;
        this.eventWeight = eventWeight;
        this.roleWeight = roleWeight;
        this.advanceWeight = advanceWeight;
        this.usageWeight = usageWeight;
        this.fairnessWeight = fairnessWeight;
    }

    public PriorityResult calculate(EventType eventType, String role, LocalDate eventDate,
                                    String department, String clubName, long resourceId) {
        long advanceDays = Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), eventDate));
        double requesterRoleWeight = "STUDENT".equals(role) ? 1.0 : "CLUB_REPRESENTATIVE".equals(role) ? 0.85 : 0;
        long recentUsage = fairnessService.recentUsageCount(department, clubName);
        FairnessSummary fairness = fairnessService.summarize(resourceId, department, clubName);
        double score = eventWeight * eventType.getPriorityWeight() + roleWeight * requesterRoleWeight
                + advanceWeight * advanceDays - usageWeight * recentUsage
                - fairnessWeight * fairness.priorityPenalty();
        String explanation = String.format("Event weight %.2f; role weight %.2f; %d advance days; %d recent usages; fairness penalty %.1f. %s",
                eventType.getPriorityWeight(), requesterRoleWeight, advanceDays, recentUsage,
                fairness.priorityPenalty(), fairness.explanation());
        return new PriorityResult(score, advanceDays, recentUsage, fairness.priorityPenalty(), explanation);
    }

    public PriorityResult calculate(BookingRequest request, long resourceId) {
        return calculate(request.eventType(), request.requesterRole().name(), request.eventDate(),
                request.department(), request.clubName(), resourceId);
    }

    /** A neutral conflict comparison for administrators; the result is informational only. */
    public String compare(BookingRequest request, BookingRequest conflicting, long resourceId) {
        PriorityResult proposed = calculate(request, resourceId);
        PriorityResult existing = calculate(conflicting, resourceId);
        if (existing.score() >= proposed.score()) {
            return String.format("Existing request %s has priority %.2f versus %.2f. Approved bookings are never displaced automatically.",
                    conflicting.bookingCode(), existing.score(), proposed.score());
        }
        return String.format("Request %s scores %.2f versus conflicting request %s at %.2f. An administrator must decide; no booking is moved automatically.",
                request.bookingCode(), proposed.score(), conflicting.bookingCode(), existing.score());
    }
}
