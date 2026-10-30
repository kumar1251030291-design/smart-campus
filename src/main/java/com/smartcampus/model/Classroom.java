package com.smartcampus.model;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;

public class Classroom extends Resource {

    public Classroom(Long id, String name, String description, String location,
                     int capacity, AvailabilityStatus availabilityStatus) {
        super(id, name, description, location, capacity, ResourceCategory.CLASSROOM, availabilityStatus);
    }

    @Override
    public String getCapacityUnit() {
        return "seats";
    }
}
