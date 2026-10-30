package com.smartcampus.model;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;

public record BookingRequestItem(long itemId, long bookingId, long resourceId, String resourceName,
                                 ResourceCategory category, int requestedQuantity, int capacityOrQuantity,
                                 AvailabilityStatus availabilityStatus) { }
