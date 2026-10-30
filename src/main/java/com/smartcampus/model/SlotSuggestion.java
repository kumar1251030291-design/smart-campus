package com.smartcampus.model;

import java.time.LocalDate;
import java.time.LocalTime;

public record SlotSuggestion(LocalDate date, LocalTime startTime, LocalTime endTime,
                             int resourceOverlap, double score, String explanation) { }
