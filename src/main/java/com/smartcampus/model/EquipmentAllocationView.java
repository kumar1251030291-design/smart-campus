package com.smartcampus.model;

import com.smartcampus.enums.EquipmentStatus;
import com.smartcampus.enums.UserRole;

import java.time.LocalDateTime;

public record EquipmentAllocationView(long allocationId, long bookingId, String bookingCode, String requesterName,
                                      String requesterEmail, long resourceId, String resourceName,
                                      String assetTag, int allocatedQuantity, EquipmentStatus equipmentStatus,
                                      LocalDateTime issueTime, LocalDateTime expectedReturnTime,
                                      LocalDateTime actualReturnTime, String returnCondition, String conditionNote,
                                      long assignedUserId, UserRole assignedUserRole) { }
