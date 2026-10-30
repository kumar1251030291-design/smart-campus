package com.smartcampus.service;

import com.smartcampus.model.FairnessSummary;
import com.smartcampus.model.ResourceUsageRecord;
import com.smartcampus.repository.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FairUsageAutoBalancingService {
    private final BookingRepository bookingRepository;
    private final double threshold;

    public FairUsageAutoBalancingService(BookingRepository bookingRepository,
            @Value("${smartcampus.fairness.threshold:0.40}") double threshold) {
        this.bookingRepository = bookingRepository;
        this.threshold = Math.min(1.0, Math.max(0.0, threshold));
    }

    public FairnessSummary summarize(long resourceId, String department, String clubName) {
        LocalDate since = LocalDate.now().minusDays(30);
        List<ResourceUsageRecord> records = bookingRepository.findUsageRecords(resourceId, since);
        double total = records.stream().mapToDouble(ResourceUsageRecord::usageHours).sum();
        double departmentHours = records.stream().filter(row -> same(row.department(), department))
                .mapToDouble(ResourceUsageRecord::usageHours).sum();
        double clubHours = records.stream().filter(row -> same(row.clubName(), clubName) && clubName != null)
                .mapToDouble(ResourceUsageRecord::usageHours).sum();
        double departmentShare = total == 0 ? 0 : departmentHours / total;
        double clubShare = total == 0 ? 0 : clubHours / total;
        double departmentExcess = Math.max(0, departmentShare - threshold);
        double clubExcess = Math.max(0, clubShare - threshold);
        double penalty = Math.max(departmentExcess, clubExcess) * 100;
        String message;
        if (penalty == 0) {
            message = "Recent usage is within the configured fairness threshold.";
        } else if (clubExcess >= departmentExcess && clubName != null) {
            message = "Club usage share is " + percent(clubShare) + "%, above the " + percent(threshold) + "% threshold.";
        } else {
            message = "Department usage share is " + percent(departmentShare) + "%, above the " + percent(threshold) + "% threshold.";
        }
        return new FairnessSummary(departmentShare, clubShare, threshold, penalty, message);
    }

    public long recentUsageCount(String department, String clubName) {
        return bookingRepository.recentCompletedUsageCount(department, clubName, LocalDate.now().minusDays(30));
    }

    private static boolean same(String left, String right) { return left != null && left.equalsIgnoreCase(right == null ? "" : right); }
    private static long percent(double value) { return Math.round(value * 100); }
}
