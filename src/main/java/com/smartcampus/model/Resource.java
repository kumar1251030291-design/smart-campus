package com.smartcampus.model;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;

public abstract class Resource {

    private Long id;
    private String name;
    private String description;
    private String location;
    private int capacityOrQuantity;
    private final ResourceCategory category;
    private AvailabilityStatus availabilityStatus;

    protected Resource(
            Long id,
            String name,
            String description,
            String location,
            int capacityOrQuantity,
            ResourceCategory category,
            AvailabilityStatus availabilityStatus) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.capacityOrQuantity = capacityOrQuantity;
        this.category = category;
        this.availabilityStatus = availabilityStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCapacityOrQuantity() {
        return capacityOrQuantity;
    }

    public void setCapacityOrQuantity(int capacityOrQuantity) {
        this.capacityOrQuantity = capacityOrQuantity;
    }

    public ResourceCategory getCategory() {
        return category;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    public abstract String getCapacityUnit();

    public String getCapacityOrQuantityLabel() {
        return capacityOrQuantity + " " + getCapacityUnit();
    }
}
