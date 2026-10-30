package com.smartcampus.enums;

import com.smartcampus.model.Resource;

import java.util.Locale;

/** Broad catalogue collections derived from each stored resource's category and name. */
public enum ResourceDiscoveryGroup {
    CLASSROOMS("Classrooms"),
    LABORATORIES("Laboratories"),
    ENGINEERING_EQUIPMENT("Engineering equipment"),
    COMPUTER_AI_LABS("Computer and AI labs"),
    AUDITORIUM_SEMINAR_HALLS("Auditoriums and seminar halls"),
    CONFERENCE_ROOMS("Conference rooms"),
    OTHER_FACILITIES("Other facilities");

    private final String displayName;
    ResourceDiscoveryGroup(String displayName) { this.displayName = displayName; }
    public String getDisplayName() { return displayName; }

    public boolean matches(Resource resource) {
        String name = resource.getName().toLowerCase(Locale.ROOT);
        return switch (this) {
            case CLASSROOMS -> resource.getCategory() == ResourceCategory.CLASSROOM;
            case LABORATORIES -> resource.getCategory() == ResourceCategory.LABORATORY;
            case ENGINEERING_EQUIPMENT -> resource.getCategory() == ResourceCategory.EQUIPMENT;
            case COMPUTER_AI_LABS -> resource.getCategory() == ResourceCategory.LABORATORY
                    && containsAny(name, "computer", "programming", "artificial intelligence", "ai lab", "gpu", "nvidia");
            case AUDITORIUM_SEMINAR_HALLS -> resource.getCategory() == ResourceCategory.FACILITY
                    && containsAny(name, "auditorium", "seminar", "lecture hall");
            case CONFERENCE_ROOMS -> resource.getCategory() == ResourceCategory.FACILITY
                    && containsAny(name, "conference", "meeting room");
            case OTHER_FACILITIES -> resource.getCategory() == ResourceCategory.FACILITY
                    && !containsAny(name, "auditorium", "seminar", "lecture hall", "conference", "meeting room");
        };
    }

    private static boolean containsAny(String text, String... terms) {
        for (String term : terms) if (text.contains(term)) return true;
        return false;
    }
}
