package com.smartcampus.repository;

import com.smartcampus.model.Announcement;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class AnnouncementRepository {
    private final JdbcTemplate jdbc;
    public AnnouncementRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Announcement> latest(int max) {
        return jdbc.query("SELECT * FROM campus_announcements ORDER BY published_on DESC, announcement_id DESC LIMIT ?",
                AnnouncementRepository::map, max);
    }

    public List<Announcement> all() {
        return jdbc.query("SELECT * FROM campus_announcements ORDER BY published_on DESC, announcement_id DESC",
                AnnouncementRepository::map);
    }

    public Optional<Announcement> find(long id) {
        return jdbc.query("SELECT * FROM campus_announcements WHERE announcement_id=?", AnnouncementRepository::map, id)
                .stream().findFirst();
    }

    private static Announcement map(ResultSet rs, int row) throws SQLException {
        return new Announcement(rs.getLong("announcement_id"), rs.getString("title"), rs.getString("summary"),
                rs.getString("body"), rs.getString("category"), rs.getString("image_path"),
                rs.getDate("published_on").toLocalDate());
    }
}
