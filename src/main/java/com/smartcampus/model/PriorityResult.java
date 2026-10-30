package com.smartcampus.model;

public record PriorityResult(double score, long advanceBookingDays, long recentUsageCount,
                            double fairnessPenalty, String explanation) { }
