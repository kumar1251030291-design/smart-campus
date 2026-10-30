package com.smartcampus.model;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;

public class Laboratory extends Resource {

    public Laboratory(Long id, String name, String description, String location,
                      int workstationCount, AvailabilityStatus availabilityStatus) {
        super(id, name, description, location, workstationCount, ResourceCategory.LABORATORY, availabilityStatus);
    }

    @Override
    public String getCapacityUnit() {
        return "workstations";
    }
}
