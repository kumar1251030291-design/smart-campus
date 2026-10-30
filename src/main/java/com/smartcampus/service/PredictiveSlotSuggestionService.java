package com.smartcampus.service;

import com.smartcampus.model.ResourceNeed;
import com.smartcampus.model.SlotSuggestion;
import com.smartcampus.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Ranks free future windows using observed weekday/time use; it does not make AI predictions. */
@Service
public class PredictiveSlotSuggestionService {
    private static final int LOOKBACK_WEEKS = 8;
    private final BookingRepository bookingRepository;
    private final BookingAvailabilityService availabilityService;

    public PredictiveSlotSuggestionService(BookingRepository bookingRepository,
                                           BookingAvailabilityService availabilityService) {
        this.bookingRepository = bookingRepository;
        this.availabilityService = availabilityService;
    }

    public List<SlotSuggestion> suggest(long resourceId, int quantity, int participants, int durationHours) {
        LocalDate today = LocalDate.now();
        LocalDate since = today.minusWeeks(LOOKBACK_WEEKS);
        if (bookingRepository.countHistoricalRequests(resourceId, since) < 5) return List.of();
        var resource = availabilityServiceResource(resourceId);
        if (resource == null) return List.of();
        List<ResourceNeed> needs = List.of(new ResourceNeed(resource, quantity));
        List<SlotSuggestion> candidates = new ArrayList<>();
        for (int offset = 1; offset <= 28; offset++) {
            LocalDate date = today.plusDays(offset);
            for (int hour : List.of(8, 10, 12, 14, 16)) {
                LocalTime start = LocalTime.of(hour, 0);
                LocalTime end = start.plusHours(durationHours);
                if (end.isAfter(LocalTime.of(20, 0))) continue;
                int h2Day = date.getDayOfWeek().getValue() % 7 + 1; // H2 numbers Sunday as 1.
                int uses = bookingRepository.countHistoricalSlotUse(resourceId, h2Day, start, end, since);
                double freeFrequency = Math.max(0, LOOKBACK_WEEKS - uses) / (double) LOOKBACK_WEEKS;
                if (!availabilityService.check(date, start, end, participants, needs, null).available()) continue;
                candidates.add(new SlotSuggestion(date, start, end, 1, freeFrequency,
                        String.format("Historically free in %.0f%% of matching weekday/time windows over the last %d weeks.",
                                freeFrequency * 100, LOOKBACK_WEEKS)));
            }
        }
        return candidates.stream().sorted(Comparator.comparingDouble(SlotSuggestion::score).reversed()
                        .thenComparing(SlotSuggestion::date).thenComparing(SlotSuggestion::startTime))
                .limit(3).toList();
    }

    private com.smartcampus.model.Resource availabilityServiceResource(long id) {
        return availabilityService.getResource(id).orElse(null);
    }
}
