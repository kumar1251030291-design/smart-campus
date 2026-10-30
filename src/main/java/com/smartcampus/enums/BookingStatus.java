package com.smartcampus.enums;

public enum BookingStatus {
    PENDING,
    UNDER_REVIEW,
    ADDITIONAL_INFORMATION_REQUIRED,
    ALTERNATIVE_PROPOSED,
    APPROVED,
    REJECTED,
    CANCELLED,
    COMPLETED;

    public String getDisplayName() {
        String[] words = name().toLowerCase().split("_");
        StringBuilder display = new StringBuilder();
        for (String word : words) {
            if (!display.isEmpty()) display.append(' ');
            display.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return display.toString();
    }
}
