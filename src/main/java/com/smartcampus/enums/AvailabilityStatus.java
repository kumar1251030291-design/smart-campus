package com.smartcampus.enums;

public enum AvailabilityStatus {
    AVAILABLE("Available", "available"),
    LIMITED("Limited availability", "limited"),
    UNAVAILABLE("Unavailable", "unavailable"),
    MAINTENANCE("Under maintenance", "maintenance");

    private final String displayName;
    private final String cssClass;

    AvailabilityStatus(String displayName, String cssClass) {
        this.displayName = displayName;
        this.cssClass = cssClass;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCssClass() {
        return cssClass;
    }
}
