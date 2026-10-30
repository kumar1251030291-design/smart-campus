package com.smartcampus.enums;

public enum EventType {
    ACADEMIC(1.00),
    TECHNICAL(0.90),
    WORKSHOP(0.80),
    CLUB_EVENT(0.65),
    OTHER(0.50);

    private final double priorityWeight;
    EventType(double priorityWeight) { this.priorityWeight = priorityWeight; }
    public double getPriorityWeight() { return priorityWeight; }
    public String getDisplayName() { return name().toLowerCase().replace('_', ' '); }
}
