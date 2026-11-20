package com.smartcampus.repository;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.model.RecentResourceView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ResourceViewRepository {
    private final JdbcTemplate jdbc;
    public ResourceViewRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void record(long userId, long resourceId) {
        jdbc.update("INSERT INTO resource_views(user_id, resource_id) VALUES(?, ?)", userId, resourceId);
    }

    /** Distinct resources, most recently viewed first. */
    public List<RecentResourceView> recentForUser(long userId, int max) {
        return jdbc.query("""
                SELECT r.id, r.name, r.category, r.location, r.availability_status, v.last_viewed
                FROM (SELECT resource_id, MAX(viewed_at) AS last_viewed FROM resource_views WHERE user_id=? GROUP BY resource_id) v
                JOIN campus_resources r ON r.id = v.resource_id
                ORDER BY v.last_viewed DESC LIMIT ?
                """, (rs, row) -> new RecentResourceView(rs.getLong("id"), rs.getString("name"),
                ResourceCategory.valueOf(rs.getString("category")), rs.getString("location"),
                AvailabilityStatus.valueOf(rs.getString("availability_status")),
                rs.getTimestamp("last_viewed").toLocalDateTime()), userId, max);
    }
}
