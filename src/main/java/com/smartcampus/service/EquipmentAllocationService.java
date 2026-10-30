package com.smartcampus.service;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.EquipmentStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.enums.ReturnCondition;
import com.smartcampus.enums.NotificationType;
import com.smartcampus.model.BookingRequest;
import com.smartcampus.model.BookingRequestItem;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.EquipmentAllocationView;
import com.smartcampus.repository.BookingRepository;
import com.smartcampus.repository.EquipmentRepository;
import com.smartcampus.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EquipmentAllocationService {
    private final EquipmentRepository equipmentRepository;
    private final BookingRepository bookingRepository;
    private final ResourceRepository resourceRepository;
    private final PortalNotificationService notifications;

    public EquipmentAllocationService(EquipmentRepository equipmentRepository, BookingRepository bookingRepository,
                                      ResourceRepository resourceRepository, PortalNotificationService notifications) {
        this.equipmentRepository = equipmentRepository;
        this.bookingRepository = bookingRepository;
        this.resourceRepository = resourceRepository;
        this.notifications = notifications;
    }

    @Transactional
    public void reserveApprovedRequest(BookingRequest booking, List<BookingRequestItem> items) {
        for (BookingRequestItem item : items) {
            if (item.category() != ResourceCategory.EQUIPMENT) continue;
            var resource = resourceRepository.findById(item.resourceId()).orElseThrow();
            List<com.smartcampus.model.EquipmentAsset> assets = equipmentRepository.availableForBooking(item.resourceId(), booking);
            int registered = equipmentRepository.countAssets(item.resourceId());
            int untrackedCapacity = Math.max(0, resource.getCapacityOrQuantity() - registered);
            int assetQuantity = Math.min(item.requestedQuantity(), assets.size());
            int quantityOnly = item.requestedQuantity() - assetQuantity;
            if (quantityOnly > untrackedCapacity) {
                throw new BookingConflictException("Not enough individually available equipment or untracked quantity remains for " + item.resourceName() + ".");
            }
            for (int index = 0; index < assetQuantity; index++) {
                equipmentRepository.reserveAsset(booking.bookingId(), item.itemId(), item.resourceId(),
                        assets.get(index).assetTag(), booking.requesterId());
            }
            equipmentRepository.reserveQuantity(booking.bookingId(), item.itemId(), item.resourceId(),
                    quantityOnly, booking.requesterId());
        }
    }

    @Transactional
    public boolean issue(long allocationId, CampusUser admin, LocalDateTime expectedReturnTime) {
        requireAdmin(admin);
        EquipmentAllocationView allocation = equipmentRepository.findAllocation(allocationId).orElseThrow();
        BookingRequest booking = bookingRepository.findByCode(allocation.bookingCode()).orElseThrow();
        if (booking.status() != BookingStatus.APPROVED || expectedReturnTime == null
                || !expectedReturnTime.isAfter(LocalDateTime.now())) return false;
        boolean issued = equipmentRepository.issue(allocationId, admin.getUserId(), expectedReturnTime);
        if (issued) equipmentRepository.findAllocation(allocationId).ifPresent(updatedAllocation ->
                notifications.equipment(allocationId, updatedAllocation.bookingId(), updatedAllocation.assignedUserId(),
                        updatedAllocation.assignedUserRole(), updatedAllocation.resourceName(), NotificationType.EQUIPMENT_ISSUED,
                        "Equipment issued", updatedAllocation.resourceName() + " has been issued for " + updatedAllocation.bookingCode() + "."));
        return issued;
    }

    @Transactional
    public boolean receive(long allocationId, CampusUser admin, ReturnCondition condition, String note) {
        requireAdmin(admin);
        if (condition == null) throw new IllegalArgumentException("Choose an equipment return condition.");
        if (note != null && note.length() > 500) throw new IllegalArgumentException("Condition notes must be 500 characters or fewer.");
        boolean received = equipmentRepository.receive(allocationId, admin.getUserId(), condition, note);
        if (received) equipmentRepository.findAllocation(allocationId).ifPresent(allocation ->
                notifications.equipment(allocationId, allocation.bookingId(), allocation.assignedUserId(),
                        allocation.assignedUserRole(), allocation.resourceName(), NotificationType.EQUIPMENT_RETURNED,
                        "Equipment returned", allocation.resourceName() + " was returned in " + condition.name().toLowerCase(java.util.Locale.ROOT) + " condition."));
        return received;
    }

    public List<EquipmentAllocationView> forBooking(long bookingId) {
        return equipmentRepository.findAllocationsForBooking(bookingId);
    }

    public List<EquipmentAllocationView> search(String search, EquipmentStatus status) {
        return equipmentRepository.searchAllocations(search, status);
    }

    public List<com.smartcampus.model.EquipmentAsset> assets() { return equipmentRepository.findAllAssets(); }

    public void releaseReservations(long bookingId) { equipmentRepository.releaseReservations(bookingId); }

    public boolean allReturned(long bookingId) { return equipmentRepository.allEquipmentReturned(bookingId); }

    private static void requireAdmin(CampusUser admin) {
        if (admin == null || admin.getRole().name().equals("ADMIN") == false) {
            throw new SecurityException("Only an administrator can issue or receive equipment.");
        }
    }

    public static class BookingConflictException extends RuntimeException {
        public BookingConflictException(String message) { super(message); }
    }
}
