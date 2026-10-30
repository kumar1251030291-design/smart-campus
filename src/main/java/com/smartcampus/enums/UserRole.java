package com.smartcampus.enums;

public enum UserRole {
    STUDENT,
    CLUB_REPRESENTATIVE,
    ADMIN;

    public String getDisplayName() {
        return switch (this) {
            case STUDENT -> "Student";
            case CLUB_REPRESENTATIVE -> "Club representative";
            case ADMIN -> "Administrator";
        };
    }
}
