package com.smartcampus.model;

import java.time.LocalDateTime;

public record ResourceUsageRecord(String department, String clubName, double usageHours,
                                  LocalDateTime recordedAt) { }
