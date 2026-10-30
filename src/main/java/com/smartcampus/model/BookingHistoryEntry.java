package com.smartcampus.model;

import com.smartcampus.enums.BookingStatus;

import java.time.LocalDateTime;

public record BookingHistoryEntry(long historyId, BookingStatus statusAfter, String action, String note,
                                  String responsibleName, LocalDateTime changedAt) { }
