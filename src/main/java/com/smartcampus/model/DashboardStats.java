package com.smartcampus.model;

/** Actual database totals used by the dashboard cards and simple CSS bar charts. */
public record DashboardStats(long totalResources, long classrooms, long laboratories, long equipment,
                             long facilities, long students, long clubRepresentatives,
                             long activeAccounts, long inactiveAccounts) {
    public int classroomsPercent() { return percent(classrooms, totalResources); }
    public int laboratoriesPercent() { return percent(laboratories, totalResources); }
    public int equipmentPercent() { return percent(equipment, totalResources); }
    public int facilitiesPercent() { return percent(facilities, totalResources); }
    public int studentsPercent() { return percent(students, students + clubRepresentatives); }
    public int clubsPercent() { return percent(clubRepresentatives, students + clubRepresentatives); }
    public int activePercent() { return percent(activeAccounts, activeAccounts + inactiveAccounts); }
    public int inactivePercent() { return percent(inactiveAccounts, activeAccounts + inactiveAccounts); }

    private int percent(long value, long total) {
        return total == 0 ? 0 : (int) Math.round(value * 100.0 / total);
    }
}
