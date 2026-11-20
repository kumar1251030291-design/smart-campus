package com.smartcampus.repository;

import com.smartcampus.enums.BookingStatus;
import com.smartcampus.enums.EventType;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.enums.UserRole;
import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.model.BookingHistoryEntry;
import com.smartcampus.model.BookingRequest;
import com.smartcampus.model.BookingRequestForm;
import com.smartcampus.model.BookingRequestItem;
import com.smartcampus.model.Resource;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.ResourceUsageRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Repository
public class BookingRepository {
    private static final String ACTIVE_BOOKING_STATUSES = "('PENDING','UNDER_REVIEW','ADDITIONAL_INFORMATION_REQUIRED','ALTERNATIVE_PROPOSED','APPROVED')";
    private final JdbcTemplate jdbc;

    public BookingRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** H2 stores identifiers in upper case, PostgreSQL in lower case; generated-key column names must match. */
    private static String keyColumn(java.sql.Connection connection, String column) throws SQLException {
        return connection.getMetaData().storesUpperCaseIdentifiers() ? column.toUpperCase(java.util.Locale.ROOT) : column;
    }

    public long insertRequest(CampusUser requester, BookingRequestForm form) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO booking_requests (requester_id, requester_name, requester_role, department, club_name,
                        event_title, event_type, purpose, event_date, start_time, end_time, expected_participants,
                        special_requirements, supporting_information, status)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
                    """, new String[]{keyColumn(connection, "booking_id")});
            statement.setLong(1, requester.getUserId());
            statement.setString(2, requester.getFullName());
            statement.setString(3, requester.getRole().name());
            statement.setString(4, requester.getDepartment());
            statement.setString(5, requester.getClubName());
            statement.setString(6, form.getEventTitle().trim());
            statement.setString(7, form.getEventType());
            statement.setString(8, form.getPurpose().trim());
            statement.setDate(9, Date.valueOf(form.getEventDate()));
            statement.setTime(10, Time.valueOf(form.getStartTime()));
            statement.setTime(11, Time.valueOf(form.getEndTime()));
            statement.setInt(12, form.getExpectedParticipants());
            statement.setString(13, clean(form.getSpecialRequirements()));
            statement.setString(14, clean(form.getSupportingInformation()));
            return statement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new IllegalStateException("The database did not return a booking ID.");
        long id = key.longValue();
        String code = "SC-" + LocalDate.now().getYear() + "-" + String.format("%06d", id);
        jdbc.update("UPDATE booking_requests SET booking_code = ? WHERE booking_id = ?", code, id);
        return id;
    }

    public void insertItems(long bookingId, BookingRequestForm form, List<Resource> resources) {
        for (int index = 0; index < resources.size(); index++) {
            jdbc.update("INSERT INTO booking_request_items (booking_id, resource_id, requested_quantity) VALUES (?, ?, ?)",
                    bookingId, resources.get(index).getId(), form.getRequestedQuantities().get(index));
        }
    }

    public long addHistory(long bookingId, BookingStatus status, String action, String note, long changedBy) {
        org.springframework.jdbc.support.KeyHolder key = new org.springframework.jdbc.support.GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO booking_history (booking_id, status_after, action, note, changed_by) VALUES (?, ?, ?, ?, ?)",
                    new String[]{keyColumn(connection, "history_id")});
            statement.setLong(1, bookingId); statement.setString(2, status.name()); statement.setString(3, action);
            statement.setString(4, clean(note)); statement.setLong(5, changedBy); return statement;
        }, key);
        Number id = key.getKey();
        if (id == null) throw new IllegalStateException("Database did not return a booking history ID.");
        return id.longValue();
    }

    public Optional<BookingRequest> findById(long id) {
        return jdbc.query("SELECT * FROM booking_requests WHERE booking_id = ?", BookingRepository::mapRequest, id)
                .stream().findFirst();
    }

    public Optional<BookingRequest> findByCode(String code) {
        return jdbc.query("SELECT * FROM booking_requests WHERE UPPER(booking_code) = UPPER(?)",
                BookingRepository::mapRequest, code.trim()).stream().findFirst();
    }

    public List<BookingRequest> findForRequester(long requesterId) {
        return jdbc.query("SELECT * FROM booking_requests WHERE requester_id = ? ORDER BY created_at DESC",
                BookingRepository::mapRequest, requesterId);
    }

    public List<BookingRequest> searchAll(String search, BookingStatus status, ResourceCategory category, LocalDate eventDate) {
        StringBuilder sql = new StringBuilder("SELECT br.* FROM booking_requests br WHERE 1=1");
        List<Object> params = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            String query = "%" + search.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(br.booking_code) LIKE ? OR LOWER(br.requester_name) LIKE ? OR LOWER(br.department) LIKE ?")
                    .append(" OR EXISTS (SELECT 1 FROM booking_request_items bi JOIN campus_resources cr ON cr.id=bi.resource_id")
                    .append(" WHERE bi.booking_id=br.booking_id AND LOWER(cr.name) LIKE ?))");
            params.add(query); params.add(query); params.add(query); params.add(query);
        }
        if (status != null) { sql.append(" AND br.status = ?"); params.add(status.name()); }
        if (category != null) {
            sql.append(" AND EXISTS (SELECT 1 FROM booking_request_items bi JOIN campus_resources cr ON cr.id=bi.resource_id")
                    .append(" WHERE bi.booking_id=br.booking_id AND cr.category=?)");
            params.add(category.name());
        }
        if (eventDate != null) { sql.append(" AND br.event_date = ?"); params.add(Date.valueOf(eventDate)); }
        sql.append(" ORDER BY br.created_at DESC");
        return jdbc.query(sql.toString(), BookingRepository::mapRequest, params.toArray());
    }

    public List<BookingRequestItem> findItems(long bookingId) {
        return jdbc.query("""
                SELECT bi.item_id, bi.booking_id, bi.resource_id, bi.requested_quantity,
                       cr.name, cr.category, cr.capacity_or_quantity, cr.availability_status
                FROM booking_request_items bi JOIN campus_resources cr ON cr.id = bi.resource_id
                WHERE bi.booking_id = ? ORDER BY cr.name
                """, (rs, row) -> new BookingRequestItem(rs.getLong("item_id"), rs.getLong("booking_id"),
                rs.getLong("resource_id"), rs.getString("name"), ResourceCategory.valueOf(rs.getString("category")),
                rs.getInt("requested_quantity"), rs.getInt("capacity_or_quantity"),
                AvailabilityStatus.valueOf(rs.getString("availability_status"))), bookingId);
    }

    public List<BookingHistoryEntry> findHistory(long bookingId) {
        return jdbc.query("""
                SELECT h.history_id, h.status_after, h.action, h.note, h.changed_at, u.full_name AS actor_name
                FROM booking_history h JOIN campus_users u ON u.user_id = h.changed_by
                WHERE h.booking_id = ? ORDER BY h.changed_at, h.history_id
                """, (rs, row) -> new BookingHistoryEntry(rs.getLong("history_id"),
                BookingStatus.valueOf(rs.getString("status_after")), rs.getString("action"), rs.getString("note"),
                rs.getString("actor_name"), timestamp(rs, "changed_at")), bookingId);
    }

    public boolean changeStatus(long bookingId, BookingStatus expected, BookingStatus next, String note,
                                LocalDate alternativeDate, LocalTime alternativeStart, LocalTime alternativeEnd) {
        return jdbc.update("""
                UPDATE booking_requests SET status = ?, decision_note = ?, alternative_date = ?,
                    alternative_start_time = ?, alternative_end_time = ?, updated_at = CURRENT_TIMESTAMP
                WHERE booking_id = ? AND status = ?
                """, next.name(), clean(note), alternativeDate == null ? null : Date.valueOf(alternativeDate),
                alternativeStart == null ? null : Time.valueOf(alternativeStart),
                alternativeEnd == null ? null : Time.valueOf(alternativeEnd), bookingId, expected.name()) > 0;
    }

    public boolean addRequesterInformation(long bookingId, String information) {
        return jdbc.update("""
                UPDATE booking_requests SET supporting_information =
                    CASE WHEN supporting_information IS NULL OR supporting_information = '' THEN ?
                         ELSE supporting_information || CHAR(10) || ? END,
                    status='UNDER_REVIEW', updated_at=CURRENT_TIMESTAMP
                WHERE booking_id=? AND status='ADDITIONAL_INFORMATION_REQUIRED'
                """, information.trim(), information.trim(), bookingId) > 0;
    }

    public boolean acceptAlternative(long bookingId) {
        return jdbc.update("""
                UPDATE booking_requests SET event_date=alternative_date, start_time=alternative_start_time,
                    end_time=alternative_end_time, alternative_date=NULL, alternative_start_time=NULL,
                    alternative_end_time=NULL, status='UNDER_REVIEW', updated_at=CURRENT_TIMESTAMP
                WHERE booking_id=? AND status='ALTERNATIVE_PROPOSED' AND alternative_date IS NOT NULL
                """, bookingId) > 0;
    }

    public void lockResources(Collection<Long> resourceIds) {
        resourceIds.stream().distinct().sorted().forEach(id -> jdbc.queryForObject(
                "SELECT id FROM campus_resources WHERE id = ? FOR UPDATE", Long.class, id));
    }

    public List<BookingRequest> findConflicts(long resourceId, LocalDate date, LocalTime start, LocalTime end,
                                              Long excludeBookingId) {
        return jdbc.query("""
                SELECT DISTINCT br.* FROM booking_requests br JOIN booking_request_items bi ON bi.booking_id = br.booking_id
                WHERE bi.resource_id = ? AND br.event_date = ? AND br.status IN """ + ACTIVE_BOOKING_STATUSES + """
                  AND br.start_time < ? AND br.end_time > ? AND (? IS NULL OR br.booking_id <> ?)
                ORDER BY br.start_time
                """, BookingRepository::mapRequest, resourceId, Date.valueOf(date), Time.valueOf(end),
                Time.valueOf(start), excludeBookingId, excludeBookingId);
    }

    public int sumOverlappingQuantity(long resourceId, LocalDate date, LocalTime start, LocalTime end, Long excludeId) {
        Integer count = jdbc.queryForObject("""
                SELECT COALESCE(SUM(bi.requested_quantity), 0)
                FROM booking_requests br JOIN booking_request_items bi ON bi.booking_id = br.booking_id
                WHERE bi.resource_id = ? AND br.event_date = ? AND br.status IN """ + ACTIVE_BOOKING_STATUSES + """
                  AND br.start_time < ? AND br.end_time > ? AND (? IS NULL OR br.booking_id <> ?)
                """, Integer.class, resourceId, Date.valueOf(date), Time.valueOf(end), Time.valueOf(start), excludeId, excludeId);
        return count == null ? 0 : count;
    }

    public int sumOverlappingParticipants(long resourceId, LocalDate date, LocalTime start, LocalTime end, Long excludeId) {
        Integer count = jdbc.queryForObject("""
                SELECT COALESCE(SUM(br.expected_participants), 0)
                FROM booking_requests br JOIN booking_request_items bi ON bi.booking_id = br.booking_id
                WHERE bi.resource_id = ? AND br.event_date = ? AND br.status IN """ + ACTIVE_BOOKING_STATUSES + """
                  AND br.start_time < ? AND br.end_time > ? AND (? IS NULL OR br.booking_id <> ?)
                """, Integer.class, resourceId, Date.valueOf(date), Time.valueOf(end), Time.valueOf(start), excludeId, excludeId);
        return count == null ? 0 : count;
    }

    public long recentCompletedUsageCount(String department, String clubName, LocalDate since) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(*) FROM resource_usage_history WHERE department = ?
                  AND COALESCE(club_name, '') = COALESCE(?, '') AND recorded_at >= ?
                """, Long.class, department, clubName, Timestamp.valueOf(since.atStartOfDay()));
        return count == null ? 0 : count;
    }

    public List<BookingRequest> historicalRequests(long resourceId, LocalDate since) {
        return jdbc.query("""
                SELECT DISTINCT br.* FROM booking_requests br JOIN booking_request_items bi ON bi.booking_id=br.booking_id
                WHERE bi.resource_id=? AND br.event_date >= ? AND br.status IN ('APPROVED','COMPLETED')
                ORDER BY br.event_date DESC
                """, BookingRepository::mapRequest, resourceId, Date.valueOf(since));
    }

    public long countHistoricalRequests(long resourceId, LocalDate since) {
        Long count = jdbc.queryForObject("""
                SELECT COUNT(DISTINCT br.booking_id) FROM booking_requests br
                JOIN booking_request_items bi ON bi.booking_id=br.booking_id
                WHERE bi.resource_id=? AND br.event_date>=? AND br.event_date<CURRENT_DATE
                  AND br.status IN ('APPROVED','COMPLETED')
                """, Long.class, resourceId, Date.valueOf(since));
        return count == null ? 0 : count;
    }

    public int countHistoricalSlotUse(long resourceId, int h2DayOfWeek, LocalTime start, LocalTime end, LocalDate since) {
        // Weekday filtering happens in Java so the query runs unchanged on H2 and PostgreSQL.
        java.time.DayOfWeek weekday = java.time.DayOfWeek.of(h2DayOfWeek == 1 ? 7 : h2DayOfWeek - 1);
        List<LocalDate> dates = jdbc.query("""
                SELECT DISTINCT br.booking_id, br.event_date FROM booking_requests br
                JOIN booking_request_items bi ON bi.booking_id=br.booking_id
                WHERE bi.resource_id=? AND br.event_date>=? AND br.event_date<CURRENT_DATE
                  AND br.start_time < ? AND br.end_time > ?
                  AND br.status IN ('APPROVED','COMPLETED')
                """, (rs, row) -> rs.getDate("event_date").toLocalDate(), resourceId, Date.valueOf(since),
                Time.valueOf(end), Time.valueOf(start));
        return (int) dates.stream().filter(date -> date.getDayOfWeek() == weekday).count();
    }

    public List<ResourceUsageRecord> findUsageRecords(long resourceId, LocalDate since) {
        return jdbc.query("""
                SELECT department, club_name, usage_hours, recorded_at
                FROM resource_usage_history WHERE resource_id=? AND recorded_at>=?
                """, (rs, row) -> new ResourceUsageRecord(rs.getString("department"), rs.getString("club_name"),
                rs.getDouble("usage_hours"), timestamp(rs, "recorded_at")), resourceId,
                Timestamp.valueOf(since.atStartOfDay()));
    }

    public void recordCompletedUsage(BookingRequest request, List<BookingRequestItem> items) {
        for (BookingRequestItem item : items) {
            double hours = java.time.Duration.between(request.startTime(), request.endTime()).toMinutes() / 60.0;
            Timestamp startAt = Timestamp.valueOf(LocalDateTime.of(request.eventDate(), request.startTime()));
            Timestamp endAt = Timestamp.valueOf(LocalDateTime.of(request.eventDate(), request.endTime()));
            // Portable upsert (works on H2 and PostgreSQL): update the row if it exists, otherwise insert it.
            int updated = jdbc.update("""
                    UPDATE resource_usage_history SET requester_role=?, department=?, club_name=?, start_at=?, end_at=?,
                        usage_hours=?, recorded_at=CURRENT_TIMESTAMP WHERE booking_id=? AND resource_id=?
                    """, request.requesterRole().name(), request.department(), request.clubName(), startAt, endAt, hours,
                    request.bookingId(), item.resourceId());
            if (updated == 0) {
                jdbc.update("""
                        INSERT INTO resource_usage_history (booking_id, resource_id, requester_role, department, club_name,
                            start_at, end_at, usage_hours, recorded_at)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
                        """, request.bookingId(), item.resourceId(), request.requesterRole().name(), request.department(),
                        request.clubName(), startAt, endAt, hours);
            }
        }
    }

    private static BookingRequest mapRequest(ResultSet rs, int row) throws SQLException {
        return new BookingRequest(rs.getLong("booking_id"), rs.getString("booking_code"), rs.getLong("requester_id"),
                rs.getString("requester_name"), UserRole.valueOf(rs.getString("requester_role")),
                rs.getString("department"), rs.getString("club_name"), rs.getString("event_title"),
                EventType.valueOf(rs.getString("event_type")), rs.getString("purpose"),
                rs.getDate("event_date").toLocalDate(), rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(), rs.getInt("expected_participants"),
                rs.getString("special_requirements"), rs.getString("supporting_information"),
                BookingStatus.valueOf(rs.getString("status")), rs.getString("decision_note"),
                localDate(rs, "alternative_date"), localTime(rs, "alternative_start_time"),
                localTime(rs, "alternative_end_time"), timestamp(rs, "created_at"), timestamp(rs, "updated_at"));
    }

    private static LocalDate localDate(ResultSet rs, String column) throws SQLException {
        Date value = rs.getDate(column); return value == null ? null : value.toLocalDate();
    }
    private static LocalTime localTime(ResultSet rs, String column) throws SQLException {
        Time value = rs.getTime(column); return value == null ? null : value.toLocalTime();
    }
    private static LocalDateTime timestamp(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column); return value == null ? null : value.toLocalDateTime();
    }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
