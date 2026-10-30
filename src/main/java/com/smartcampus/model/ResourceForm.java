package com.smartcampus.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ResourceForm {
    @NotBlank(message = "Enter a resource name.")
    @Size(max = 120, message = "Name must be 120 characters or fewer.")
    private String name;
    @NotBlank(message = "Enter a description.")
    @Size(max = 500, message = "Description must be 500 characters or fewer.")
    private String description;
    @NotBlank(message = "Enter a location.")
    @Size(max = 200, message = "Location must be 200 characters or fewer.")
    private String location;
    @Min(value = 1, message = "Capacity or quantity must be at least 1.")
    private int capacityOrQuantity;
    @NotBlank(message = "Choose a resource category.")
    @Pattern(regexp = "CLASSROOM|LABORATORY|EQUIPMENT|FACILITY", message = "Choose a valid category.")
    private String category;
    @NotBlank(message = "Choose an availability status.")
    @Pattern(regexp = "AVAILABLE|LIMITED|UNAVAILABLE|MAINTENANCE", message = "Choose a valid availability status.")
    private String availabilityStatus;

    public ResourceForm() { }

    public ResourceForm(Resource resource) {
        name = resource.getName();
        description = resource.getDescription();
        location = resource.getLocation();
        capacityOrQuantity = resource.getCapacityOrQuantity();
        category = resource.getCategory().name();
        availabilityStatus = resource.getAvailabilityStatus().name();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public int getCapacityOrQuantity() { return capacityOrQuantity; }
    public void setCapacityOrQuantity(int capacityOrQuantity) { this.capacityOrQuantity = capacityOrQuantity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAvailabilityStatus() { return availabilityStatus; }
    public void setAvailabilityStatus(String availabilityStatus) { this.availabilityStatus = availabilityStatus; }
}
