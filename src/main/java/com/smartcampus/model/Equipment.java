package com.smartcampus.model;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;

public class Equipment extends Resource {

    public Equipment(Long id, String name, String description, String location,
                     int quantity, AvailabilityStatus availabilityStatus) {
        super(id, name, description, location, quantity, ResourceCategory.EQUIPMENT, availabilityStatus);
    }

    @Override
    public String getCapacityUnit() {
        return "items";
    }
}
