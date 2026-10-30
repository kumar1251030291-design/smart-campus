package com.smartcampus.model;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.EventType;
import com.smartcampus.enums.UserRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BookingRequest(long bookingId, String bookingCode, long requesterId, String requesterName,
                             UserRole requesterRole, String department, String clubName,
                             String eventTitle, EventType eventType, String purpose, LocalDate eventDate,
                             LocalTime startTime, LocalTime endTime, int expectedParticipants,
                             String specialRequirements, String supportingInformation, BookingStatus status,
                             String decisionNote, LocalDate alternativeDate, LocalTime alternativeStartTime,
                             LocalTime alternativeEndTime, LocalDateTime createdAt, LocalDateTime updatedAt) { }
