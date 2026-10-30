package com.smartcampus.enums;

public enum ResourceCategory {
    CLASSROOM("Classroom"),
    LABORATORY("Laboratory"),
    EQUIPMENT("Equipment"),
    FACILITY("Event facility");

    private final String displayName;

    ResourceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
