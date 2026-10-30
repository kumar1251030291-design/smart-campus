package com.smartcampus.model;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;

public class Facility extends Resource {

    public Facility(Long id, String name, String description, String location,
                    int capacity, AvailabilityStatus availabilityStatus) {
        super(id, name, description, location, capacity, ResourceCategory.FACILITY, availabilityStatus);
    }

    @Override
    public String getCapacityUnit() {
        return "people";
    }
}
