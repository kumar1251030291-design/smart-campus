package com.smartcampus.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashMap;
import java.util.Map;

/** What a club representative or administrator types when posting an announcement. */
public class AnnouncementForm {
    /** The only categories and pictures a post may use (keeps image paths safe). */
    public static final Map<String, String> CATEGORIES = new LinkedHashMap<>();
    public static final Map<String, String> IMAGES = new LinkedHashMap<>();
    static {
        CATEGORIES.put("EVENT", "Event");
        CATEGORIES.put("FACILITY", "Facility");
        CATEGORIES.put("EQUIPMENT", "Equipment");
        CATEGORIES.put("MAINTENANCE", "Maintenance");
        CATEGORIES.put("POLICY", "Policy");
        IMAGES.put("/images/announcements/plain-tech-fest.svg", "Tech / code (green)");
        IMAGES.put("/images/announcements/plain-cultural-night.svg", "Music / cultural (purple)");
        IMAGES.put("/images/announcements/plain-gpu-lab.svg", "Lab / computer chip (dark green)");
        IMAGES.put("/images/announcements/plain-laptops.svg", "Laptop / equipment (teal)");
        IMAGES.put("/images/announcements/plain-maintenance.svg", "Maintenance / gear (amber)");
        IMAGES.put("/images/announcements/plain-guidelines.svg", "Notice / checklist (blue)");
    }

    @NotBlank(message = "Enter a title.")
    @Size(min = 5, max = 160, message = "The title must be 5 to 160 characters.")
    private String title;

    @NotBlank(message = "Enter a short summary.")
    @Size(min = 10, max = 600, message = "The summary must be 10 to 600 characters.")
    private String summary;

    @Size(max = 2000, message = "The details must be 2000 characters or fewer.")
    private String body;

    @NotBlank(message = "Choose a category.")
    @Pattern(regexp = "EVENT|FACILITY|EQUIPMENT|MAINTENANCE|POLICY", message = "Choose a valid category.")
    private String category = "EVENT";

    @NotBlank(message = "Choose a picture.")
    private String imagePath = "/images/announcements/plain-tech-fest.svg";

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
}
