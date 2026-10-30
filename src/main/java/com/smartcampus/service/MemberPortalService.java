package com.smartcampus.service;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.EquipmentStatus;
import com.smartcampus.model.BookingRequest;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.EquipmentAllocationView;
import com.smartcampus.model.MemberDashboardData;
import com.smartcampus.repository.BookingRepository;
import com.smartcampus.repository.EquipmentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class MemberPortalService {
    private final BookingRepository bookingRepository;
    private final EquipmentRepository equipmentRepository;
    private final PortalNotificationService notificationService;

    public MemberPortalService(BookingRepository bookingRepository, EquipmentRepository equipmentRepository,
                               PortalNotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.equipmentRepository = equipmentRepository;
        this.notificationService = notificationService;
    }

    public MemberDashboardData dashboard(CampusUser user) {
        List<BookingRequest> requests = bookingRepository.findForRequester(user.getUserId());
        LocalDateTime now = LocalDateTime.now();
        List<BookingRequest> upcoming = requests.stream()
                .filter(request -> request.status() == BookingStatus.APPROVED)
                .filter(request -> LocalDateTime.of(request.eventDate(), request.startTime()).isAfter(now))
                .sorted(Comparator.comparing(BookingRequest::eventDate).thenComparing(BookingRequest::startTime)).toList();
        List<EquipmentAllocationView> allEquipment = equipmentRepository.findAllocationsForUser(user.getUserId());
        List<EquipmentAllocationView> activeEquipment = allEquipment.stream()
                .filter(item -> item.equipmentStatus() == EquipmentStatus.RESERVED || item.equipmentStatus() == EquipmentStatus.ISSUED).toList();
        return new MemberDashboardData(requests.size(), count(requests, BookingStatus.PENDING),
                count(requests, BookingStatus.APPROVED), count(requests, BookingStatus.REJECTED),
                requests.stream().limit(5).toList(), upcoming.stream().limit(5).toList(), activeEquipment,
                notificationService.recentForUser(user.getUserId(), 8));
    }

    public List<EquipmentAllocationView> activeEquipment(long userId) { return equipmentRepository.findActiveAllocationsForUser(userId); }
    public List<EquipmentAllocationView> equipmentHistory(long userId) { return equipmentRepository.findAllocationsForUser(userId); }

    private static long count(List<BookingRequest> requests, BookingStatus status) {
        return requests.stream().filter(request -> request.status() == status).count();
    }
}
