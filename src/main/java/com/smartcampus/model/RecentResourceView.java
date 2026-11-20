package com.smartcampus.model;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;

import java.time.LocalDateTime;

/** A resource detail page the member opened recently. */
public record RecentResourceView(long resourceId, String name, ResourceCategory category, String location,
                                 AvailabilityStatus availabilityStatus, LocalDateTime viewedAt) { }
