package com.smartcampus.model;

import java.time.LocalDate;

/** A campus notice shown on dashboards and the public announcements page. */
public record Announcement(long announcementId, String title, String summary, String body,
                           String category, String imagePath, LocalDate publishedOn) {
    public String categoryLabel() {
        return category == null ? "" : category.charAt(0) + category.substring(1).toLowerCase();
    }
}
