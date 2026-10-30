package com.smartcampus.model;

import com.smartcampus.enums.EquipmentStatus;

public record EquipmentAsset(String assetTag, long resourceId, String resourceName,
                             EquipmentStatus assetStatus, String notes) { }
