package com.smartcampus.service;

import com.smartcampus.model.AvailabilityResult;
import com.smartcampus.model.ResourceNeed;
import com.smartcampus.model.SlotSuggestion;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Finds common resource windows, then ranks partial-overlap fallbacks when no common slot exists. */
@Service
public class MultiResourceBundlingService {
    private final BookingAvailabilityService availabilityService;
    public MultiResourceBundlingService(BookingAvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    public List<SlotSuggestion> findAlternatives(LocalDate requestedDate, LocalTime requestedStart,
            LocalTime requestedEnd, int participants, List<ResourceNeed> needs, Long excludeBookingId) {
        if (needs == null || needs.isEmpty() || requestedStart == null || requestedEnd == null
                || !requestedEnd.isAfter(requestedStart)) return List.of();
        Duration duration = Duration.between(requestedStart, requestedEnd);
        List<SlotSuggestion> candidates = new ArrayList<>();
        for (int day = 0; day <= 14; day++) {
            LocalDate date = requestedDate.plusDays(day);
            for (int hour = 8; hour <= 18; hour++) {
                LocalTime start = LocalTime.of(hour, 0);
                LocalTime end = start.plus(duration);
                if (end.isAfter(LocalTime.of(21, 0))) continue;
                if (date.equals(requestedDate) && start.equals(requestedStart)) continue;
                int overlap = 0;
                List<String> conflicts = new ArrayList<>();
                for (ResourceNeed need : needs) {
                    AvailabilityResult result = availabilityService.check(date, start, end, participants,
                            List.of(need), excludeBookingId);
                    if (result.available()) overlap++;
                    else if (!result.issues().isEmpty()) conflicts.add(result.issues().getFirst());
                }
                long dayDistance = Math.abs(java.time.temporal.ChronoUnit.DAYS.between(requestedDate, date));
                long hourDistance = Math.abs(Duration.between(requestedStart, start).toHours());
                double score = overlap * 100.0 - dayDistance - hourDistance * 0.05;
                String explanation = overlap == needs.size()
                        ? "All requested resources share this window."
                        : "Available for " + overlap + " of " + needs.size() + " requested resources. "
                          + (conflicts.isEmpty() ? "The remaining resource capacity is limited." : conflicts.getFirst());
                candidates.add(new SlotSuggestion(date, start, end, overlap, score, explanation));
            }
        }
        return candidates.stream().sorted(Comparator.comparingInt(SlotSuggestion::resourceOverlap).reversed()
                        .thenComparing(Comparator.comparingDouble(SlotSuggestion::score).reversed())
                        .thenComparing(SlotSuggestion::date).thenComparing(SlotSuggestion::startTime))
                .limit(3).toList();
    }
}
