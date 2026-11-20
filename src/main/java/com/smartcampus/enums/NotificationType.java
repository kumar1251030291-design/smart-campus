package com.smartcampus.enums;

public enum NotificationType {
    BOOKING_SUBMITTED, BOOKING_APPROVED, BOOKING_REJECTED, INFORMATION_REQUESTED,
    ALTERNATIVE_PROPOSED, EQUIPMENT_ISSUED, EQUIPMENT_DUE, EQUIPMENT_OVERDUE,
    EQUIPMENT_RETURNED, ANNOUNCEMENT;

    public String displayName() {
        return switch (this) {
            case BOOKING_SUBMITTED -> "Booking submitted";
            case BOOKING_APPROVED -> "Booking approved";
            case BOOKING_REJECTED -> "Booking rejected";
            case INFORMATION_REQUESTED -> "Information requested";
            case ALTERNATIVE_PROPOSED -> "Alternative proposed";
            case EQUIPMENT_ISSUED -> "Equipment issued";
            case EQUIPMENT_DUE -> "Equipment due";
            case EQUIPMENT_OVERDUE -> "Equipment overdue";
            case EQUIPMENT_RETURNED -> "Equipment returned";
            case ANNOUNCEMENT -> "Announcement";
        };
    }
}
