package com.smartcampus.model;

import java.util.List;

public record MemberDashboardData(long totalBookings, long pendingBookings, long approvedBookings,
                                  long rejectedBookings, List<BookingRequest> recentBookings,
                                  List<BookingRequest> upcomingBookings,
                                  List<EquipmentAllocationView> allocatedEquipment,
                                  List<PortalNotification> notifications) { }
