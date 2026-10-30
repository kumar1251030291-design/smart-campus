package com.smartcampus.model;

import com.smartcampus.enums.NotificationType;
import java.time.LocalDateTime;

public record PortalNotification(long notificationId, long recipientUserId, String title, String message,
                                 NotificationType type, Long bookingId, String destinationUrl,
                                 LocalDateTime occurredAt, LocalDateTime readAt) {
    public boolean unread() { return readAt == null; }
    public boolean attentionNeeded() {
        return type == NotificationType.BOOKING_REJECTED || type == NotificationType.INFORMATION_REQUESTED
                || type == NotificationType.EQUIPMENT_OVERDUE;
    }
}
