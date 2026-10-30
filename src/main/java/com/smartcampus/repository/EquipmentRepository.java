package com.smartcampus.repository;

import com.smartcampus.enums.EquipmentStatus;
import com.smartcampus.enums.ReturnCondition;
import com.smartcampus.model.BookingRequest;
import com.smartcampus.model.EquipmentAllocationView;
import com.smartcampus.model.EquipmentAsset;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class EquipmentRepository {
    private final JdbcTemplate jdbc;
    public EquipmentRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public int countAssets(long resourceId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM equipment_assets WHERE resource_id=?", Integer.class, resourceId);
        return count == null ? 0 : count;
    }

    public int countUnavailableAssets(long resourceId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM equipment_assets WHERE resource_id=? AND asset_status IN ('UNDER_MAINTENANCE','LOST','DAMAGED')",
                Integer.class, resourceId);
        return count == null ? 0 : count;
    }

    public List<EquipmentAsset> availableForBooking(long resourceId, BookingRequest booking) {
        return jdbc.query("""
                SELECT a.asset_tag, a.resource_id, r.name AS resource_name, a.asset_status, a.notes
                FROM equipment_assets a JOIN campus_resources r ON r.id=a.resource_id
                WHERE a.resource_id=? AND a.asset_status IN ('AVAILABLE','RETURNED')
                  AND NOT EXISTS (
                    SELECT 1 FROM equipment_allocations ea
                    JOIN booking_requests br ON br.booking_id=ea.booking_id
                    WHERE ea.asset_tag=a.asset_tag AND ea.equipment_status IN ('RESERVED','ISSUED')
                      AND br.status='APPROVED' AND br.event_date=?
                      AND br.start_time < ? AND br.end_time > ? AND br.booking_id <> ?
                  )
                ORDER BY a.asset_tag FOR UPDATE
                """, EquipmentRepository::mapAsset, resourceId, java.sql.Date.valueOf(booking.eventDate()),
                java.sql.Time.valueOf(booking.endTime()), java.sql.Time.valueOf(booking.startTime()), booking.bookingId());
    }

    public void reserveAsset(long bookingId, long itemId, long resourceId, String assetTag, long requesterId) {
        jdbc.update("""
                INSERT INTO equipment_allocations (booking_id, item_id, resource_id, asset_tag, allocated_quantity,
                    assigned_user_id, equipment_status)
                VALUES (?, ?, ?, ?, 1, ?, 'RESERVED')
                """, bookingId, itemId, resourceId, assetTag, requesterId);
    }

    public void reserveQuantity(long bookingId, long itemId, long resourceId, int quantity, long requesterId) {
        if (quantity <= 0) return;
        jdbc.update("""
                INSERT INTO equipment_allocations (booking_id, item_id, resource_id, asset_tag, allocated_quantity,
                    assigned_user_id, equipment_status)
                VALUES (?, ?, ?, NULL, ?, ?, 'RESERVED')
                """, bookingId, itemId, resourceId, quantity, requesterId);
    }

    public List<EquipmentAllocationView> findAllocationsForBooking(long bookingId) {
        return jdbc.query(allocationSelect() + " WHERE ea.booking_id=? ORDER BY r.name, ea.asset_tag",
                EquipmentRepository::mapAllocation, bookingId);
    }

    public java.util.Optional<EquipmentAllocationView> findAllocation(long allocationId) {
        return jdbc.query(allocationSelect() + " WHERE ea.allocation_id=?", EquipmentRepository::mapAllocation, allocationId)
                .stream().findFirst();
    }

    public List<EquipmentAllocationView> searchAllocations(String search, EquipmentStatus status) {
        StringBuilder sql = new StringBuilder(allocationSelect()).append(" WHERE 1=1");
        java.util.ArrayList<Object> args = new java.util.ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(br.booking_code) LIKE ? OR LOWER(u.full_name) LIKE ? OR LOWER(r.name) LIKE ? OR LOWER(COALESCE(ea.asset_tag,'')) LIKE ?)");
            String needle = "%" + search.trim().toLowerCase() + "%";
            args.add(needle); args.add(needle); args.add(needle); args.add(needle);
        }
        if (status != null) { sql.append(" AND ea.equipment_status=?"); args.add(status.name()); }
        sql.append(" ORDER BY CASE ea.equipment_status WHEN 'ISSUED' THEN 0 WHEN 'RESERVED' THEN 1 ELSE 2 END, ea.expected_return_time");
        return jdbc.query(sql.toString(), EquipmentRepository::mapAllocation, args.toArray());
    }

    public boolean issue(long allocationId, long adminId, LocalDateTime expectedReturn) {
        String assetTag = jdbc.query("SELECT asset_tag FROM equipment_allocations WHERE allocation_id=?", rs ->
                rs.next() ? rs.getString(1) : null, allocationId);
        if (assetTag != null) {
            String status = jdbc.query("SELECT asset_status FROM equipment_assets WHERE asset_tag=? FOR UPDATE", rs ->
                    rs.next() ? rs.getString(1) : null, assetTag);
            if (!"AVAILABLE".equals(status) && !"RETURNED".equals(status)) return false;
        }
        int changed = jdbc.update("""
                UPDATE equipment_allocations SET equipment_status='ISSUED', issue_time=CURRENT_TIMESTAMP,
                    expected_return_time=?, issued_by=?
                WHERE allocation_id=? AND equipment_status='RESERVED'
                """, Timestamp.valueOf(expectedReturn), adminId, allocationId);
        if (changed > 0) jdbc.update("UPDATE equipment_assets SET asset_status='ISSUED' WHERE asset_tag=(SELECT asset_tag FROM equipment_allocations WHERE allocation_id=?)", allocationId);
        return changed > 0;
    }

    public boolean allEquipmentReturned(long bookingId) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM equipment_allocations WHERE booking_id=? AND equipment_status IN ('RESERVED','ISSUED')",
                Integer.class, bookingId);
        return count == null || count == 0;
    }

    public boolean receive(long allocationId, long adminId, ReturnCondition condition, String note) {
        int changed = jdbc.update("""
                UPDATE equipment_allocations SET equipment_status='RETURNED', actual_return_time=CURRENT_TIMESTAMP,
                    returned_by=?, return_condition=?, condition_note=?
                WHERE allocation_id=? AND equipment_status='ISSUED'
                """, adminId, condition.name(), clean(note), allocationId);
        if (changed > 0) {
            String assetStatus = switch (condition) {
                case GOOD, MINOR_DAMAGE -> EquipmentStatus.RETURNED.name();
                case DAMAGED -> EquipmentStatus.DAMAGED.name();
                case MISSING -> EquipmentStatus.LOST.name();
            };
            jdbc.update("UPDATE equipment_assets SET asset_status=? WHERE asset_tag=(SELECT asset_tag FROM equipment_allocations WHERE allocation_id=?)",
                    assetStatus, allocationId);
        }
        return changed > 0;
    }

    public void releaseReservations(long bookingId) {
        jdbc.update("UPDATE equipment_allocations SET equipment_status='AVAILABLE' WHERE booking_id=? AND equipment_status='RESERVED'", bookingId);
    }

    public List<EquipmentAsset> findAllAssets() {
        return jdbc.query("SELECT a.asset_tag,a.resource_id,r.name resource_name,a.asset_status,a.notes FROM equipment_assets a JOIN campus_resources r ON r.id=a.resource_id ORDER BY r.name,a.asset_tag",
                EquipmentRepository::mapAsset);
    }

    public List<EquipmentAsset> findAssetsForResource(long resourceId) {
        return jdbc.query("SELECT a.asset_tag,a.resource_id,r.name resource_name,a.asset_status,a.notes "
                        + "FROM equipment_assets a JOIN campus_resources r ON r.id=a.resource_id "
                        + "WHERE a.resource_id=? ORDER BY a.asset_tag",
                EquipmentRepository::mapAsset, resourceId);
    }

    public List<EquipmentAllocationView> findActiveAllocationsForUser(long userId) {
        return jdbc.query(allocationSelect() + " WHERE ea.assigned_user_id=? AND ea.equipment_status IN ('RESERVED','ISSUED') "
                        + "ORDER BY ea.expected_return_time, ea.allocation_id",
                EquipmentRepository::mapAllocation, userId);
    }

    public List<EquipmentAllocationView> findAllocationsForUser(long userId) {
        return jdbc.query(allocationSelect() + " WHERE ea.assigned_user_id=? "
                        + "ORDER BY COALESCE(ea.actual_return_time, ea.issue_time, ea.expected_return_time) DESC, ea.allocation_id DESC",
                EquipmentRepository::mapAllocation, userId);
    }

    public List<EquipmentAllocationView> issuedForReminder() {
        return jdbc.query(allocationSelect() + " WHERE ea.equipment_status='ISSUED' AND ea.expected_return_time IS NOT NULL",
                EquipmentRepository::mapAllocation);
    }

    private static String allocationSelect() {
        return """
                SELECT ea.allocation_id, ea.booking_id, br.booking_code, u.full_name requester_name, u.email requester_email,
                       ea.resource_id, r.name resource_name, ea.asset_tag, ea.allocated_quantity,
                       ea.equipment_status, ea.issue_time, ea.expected_return_time, ea.actual_return_time,
                       ea.return_condition, ea.condition_note, ea.assigned_user_id, u.role assigned_user_role
                FROM equipment_allocations ea JOIN booking_requests br ON br.booking_id=ea.booking_id
                JOIN campus_users u ON u.user_id=ea.assigned_user_id JOIN campus_resources r ON r.id=ea.resource_id
                """;
    }

    private static EquipmentAsset mapAsset(ResultSet rs, int row) throws SQLException {
        return new EquipmentAsset(rs.getString("asset_tag"), rs.getLong("resource_id"), rs.getString("resource_name"),
                EquipmentStatus.valueOf(rs.getString("asset_status")), rs.getString("notes"));
    }

    private static EquipmentAllocationView mapAllocation(ResultSet rs, int row) throws SQLException {
        return new EquipmentAllocationView(rs.getLong("allocation_id"), rs.getLong("booking_id"), rs.getString("booking_code"),
                rs.getString("requester_name"), rs.getString("requester_email"), rs.getLong("resource_id"),
                rs.getString("resource_name"), rs.getString("asset_tag"), rs.getInt("allocated_quantity"),
                EquipmentStatus.valueOf(rs.getString("equipment_status")), timestamp(rs, "issue_time"),
                timestamp(rs, "expected_return_time"), timestamp(rs, "actual_return_time"),
                rs.getString("return_condition"), rs.getString("condition_note"), rs.getLong("assigned_user_id"),
                com.smartcampus.enums.UserRole.valueOf(rs.getString("assigned_user_role")));
    }
    private static LocalDateTime timestamp(ResultSet rs, String column) throws SQLException {
        Timestamp value = rs.getTimestamp(column); return value == null ? null : value.toLocalDateTime();
    }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
