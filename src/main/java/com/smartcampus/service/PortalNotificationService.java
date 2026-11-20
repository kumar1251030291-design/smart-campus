package com.smartcampus.service;

import com.smartcampus.enums.NotificationType;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.PortalNotification;
import com.smartcampus.repository.PortalNotificationRepository;
import com.smartcampus.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class PortalNotificationService {
    private final PortalNotificationRepository notifications;
    private final UserRepository users;

    public PortalNotificationService(PortalNotificationRepository notifications, UserRepository users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional
    public void bookingSubmitted(long bookingId, long requesterId, String eventTitle, long eventId) {
        create(requesterId, "Booking request submitted", "Your request for \"" + eventTitle + "\" was sent to the campus team.",
                NotificationType.BOOKING_SUBMITTED, bookingId, "/" + memberPath(requesterId) + "/bookings/" + bookingId,
                "booking-event:" + eventId + ":user:" + requesterId);
        for (long adminId : users.findActiveAdminIds()) {
            create(adminId, "New booking request", "A campus resource request is ready for review.",
                    NotificationType.BOOKING_SUBMITTED, bookingId, "/admin/bookings/" + bookingId,
                    "booking-event:" + eventId + ":user:" + adminId);
        }
    }

    @Transactional
    public void bookingStatus(long bookingId, long requesterId, NotificationType type, String title,
                              String message, long eventId) {
        create(requesterId, title, message, type, bookingId,
                "/" + memberPath(requesterId) + "/bookings/" + bookingId,
                "booking-event:" + eventId + ":user:" + requesterId);
    }

    @Transactional
    public void equipment(long allocationId, long bookingId, long userId, UserRole role, String resourceName,
                          NotificationType type, String title, String detail) {
        String path = role == UserRole.CLUB_REPRESENTATIVE ? "club" : "student";
        create(userId, title, detail, type, bookingId, "/" + path + "/equipment",
                "equipment:" + allocationId + ":" + type.name() + ":user:" + userId);
    }

    public List<PortalNotification> recentForUser(long userId, int max) { return notifications.recentForUser(userId, max); }

    public List<PortalNotification> inboxFor(CampusUser user) {
        requireMember(user);
        return notifications.allForUser(user.getUserId());
    }

    public long unreadCountFor(CampusUser user) {
        requireMember(user);
        return notifications.unreadCountForUser(user.getUserId());
    }

    public PortalNotification findFor(CampusUser user, long notificationId) {
        requireMember(user);
        return notifications.findForUser(notificationId, user.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void markRead(CampusUser user, long notificationId) {
        requireMember(user);
        if (notifications.markReadForUser(notificationId, user.getUserId()) == 0
                && notifications.findForUser(notificationId, user.getUserId()).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public int markAllRead(CampusUser user) {
        requireMember(user);
        return notifications.markAllReadForUser(user.getUserId());
    }

    private void create(long userId, String title, String message, NotificationType type,
                        Long bookingId, String url, String eventKey) {
        notifications.create(userId, title, message, type, bookingId, url, eventKey);
    }

    private String memberPath(long userId) {
        return users.roleForUser(userId).orElse(UserRole.STUDENT) == UserRole.CLUB_REPRESENTATIVE ? "club" : "student";
    }

    private static void requireMember(CampusUser user) {
        if (user == null || (user.getRole() != UserRole.STUDENT && user.getRole() != UserRole.CLUB_REPRESENTATIVE)) {
            throw new AccessDeniedException("Only students and club representatives can access member notifications.");
        }
    }
}
