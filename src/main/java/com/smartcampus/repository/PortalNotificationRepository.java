package com.smartcampus.repository;

import com.smartcampus.enums.NotificationType;
import com.smartcampus.model.PortalNotification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class PortalNotificationRepository {
    private final JdbcTemplate jdbc;
    public PortalNotificationRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public boolean create(long recipientId, String title, String message, NotificationType type,
                          Long bookingId, String destination, String eventKey) {
        // Portable upsert keyed on event_key (works on H2 and PostgreSQL).
        int updated = jdbc.update("""
                UPDATE portal_notifications SET recipient_user_id=?, title=?, message=?, notification_type=?,
                    related_booking_id=?, destination_url=? WHERE event_key=?
                """, recipientId, title, message, type.name(), bookingId, destination, eventKey);
        if (updated > 0) return true;
        try {
            return jdbc.update("""
                    INSERT INTO portal_notifications(recipient_user_id,title,message,notification_type,related_booking_id,destination_url,event_key)
                    VALUES(?,?,?,?,?,?,?)
                    """, recipientId, title, message, type.name(), bookingId, destination, eventKey) > 0;
        } catch (org.springframework.dao.DuplicateKeyException raced) {
            return false;
        }
    }

    public List<PortalNotification> recentForUser(long userId, int max) {
        return jdbc.query("SELECT * FROM portal_notifications WHERE recipient_user_id=? ORDER BY created_at DESC, notification_id DESC LIMIT ?",
                PortalNotificationRepository::map, userId, max);
    }

    public List<PortalNotification> allForUser(long userId) {
        return jdbc.query("SELECT * FROM portal_notifications WHERE recipient_user_id=? ORDER BY created_at DESC, notification_id DESC",
                PortalNotificationRepository::map, userId);
    }

    public Optional<PortalNotification> findForUser(long notificationId, long userId) {
        return jdbc.query("SELECT * FROM portal_notifications WHERE notification_id=? AND recipient_user_id=?",
                PortalNotificationRepository::map, notificationId, userId).stream().findFirst();
    }

    public long unreadCountForUser(long userId) {
        Long count = jdbc.queryForObject("SELECT COUNT(*) FROM portal_notifications WHERE recipient_user_id=? AND read_at IS NULL",
                Long.class, userId);
        return count == null ? 0 : count;
    }

    public int markReadForUser(long notificationId, long userId) {
        return jdbc.update("UPDATE portal_notifications SET read_at=CURRENT_TIMESTAMP WHERE notification_id=? AND recipient_user_id=? AND read_at IS NULL",
                notificationId, userId);
    }

    public int markAllReadForUser(long userId) {
        return jdbc.update("UPDATE portal_notifications SET read_at=CURRENT_TIMESTAMP WHERE recipient_user_id=? AND read_at IS NULL",
                userId);
    }

    private static PortalNotification map(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        Timestamp created = rs.getTimestamp("created_at"), readAt = rs.getTimestamp("read_at");
        long raw = rs.getLong("related_booking_id"); Long booking = rs.wasNull() ? null : raw;
        return new PortalNotification(rs.getLong("notification_id"), rs.getLong("recipient_user_id"), rs.getString("title"),
                rs.getString("message"), NotificationType.valueOf(rs.getString("notification_type")), booking,
                rs.getString("destination_url"), created == null ? null : created.toLocalDateTime(),
                readAt == null ? null : readAt.toLocalDateTime());
    }
}
