package com.smartcampus.repository;

import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.model.AdminUserView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Locale;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {
    private static final String ADMIN_VIEW_COLUMNS = "SELECT user_id, full_name, email, role, department, "
            + "year_of_study, roll_number, club_name, registration_date, account_status FROM campus_users";
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) { this.jdbcTemplate = jdbcTemplate; }

    public Optional<CampusUser> findByEmail(String email) {
        return jdbcTemplate.query("SELECT * FROM campus_users WHERE email = ?", ROW_MAPPER,
                normalizeEmail(email)).stream().findFirst();
    }

    public Optional<CampusUser> findById(long userId) {
        return jdbcTemplate.query("SELECT * FROM campus_users WHERE user_id = ?", ROW_MAPPER, userId).stream().findFirst();
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM campus_users WHERE email = ?", Integer.class, normalizeEmail(email));
        return count != null && count > 0;
    }

    public Optional<AdminUserView> findAdminViewById(long id) {
        return jdbcTemplate.query(ADMIN_VIEW_COLUMNS + " WHERE user_id = ?", UserRepository::mapAdminView, id)
                .stream().findFirst();
    }

    public List<AdminUserView> searchUsers(String search, UserRole role, AccountStatus status) {
        StringBuilder sql = new StringBuilder(ADMIN_VIEW_COLUMNS + " WHERE 1 = 1");
        List<Object> parameters = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            sql.append(" AND (LOWER(full_name) LIKE ? OR LOWER(email) LIKE ?)");
            String query = "%" + search.trim().toLowerCase(Locale.ROOT) + "%";
            parameters.add(query);
            parameters.add(query);
        }
        if (role != null) {
            sql.append(" AND role = ?");
            parameters.add(role.name());
        }
        if (status != null) {
            sql.append(" AND account_status = ?");
            parameters.add(status.name());
        }
        sql.append(" ORDER BY full_name, email");
        return jdbcTemplate.query(sql.toString(), UserRepository::mapAdminView, parameters.toArray());
    }

    public long countByRole(UserRole role) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM campus_users WHERE role = ?", Long.class, role.name());
        return count == null ? 0 : count;
    }

    public List<Long> findActiveAdminIds() {
        return jdbcTemplate.queryForList("SELECT user_id FROM campus_users WHERE role='ADMIN' AND account_status='ACTIVE'", Long.class);
    }

    public Optional<UserRole> roleForUser(long userId) {
        return jdbcTemplate.query("SELECT role FROM campus_users WHERE user_id=?", (rs, row) -> UserRole.valueOf(rs.getString(1)), userId)
                .stream().findFirst();
    }

    public long countByStatus(AccountStatus status) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM campus_users WHERE account_status = ?", Long.class, status.name());
        return count == null ? 0 : count;
    }

    public boolean updateAccountStatus(long userId, AccountStatus status) {
        return jdbcTemplate.update("UPDATE campus_users SET account_status = ? WHERE user_id = ?",
                status.name(), userId) > 0;
    }

    public boolean updateStudentProfile(long userId, String fullName, String department, int yearOfStudy) {
        return jdbcTemplate.update("""
                UPDATE campus_users SET full_name=?, department=?, year_of_study=?
                WHERE user_id=? AND role='STUDENT'
                """, clean(fullName), clean(department), yearOfStudy, userId) > 0;
    }

    public boolean updateClubRepresentativeProfile(long userId, String fullName, String department, String clubName) {
        return jdbcTemplate.update("""
                UPDATE campus_users SET full_name=?, department=?, club_name=?
                WHERE user_id=? AND role='CLUB_REPRESENTATIVE'
                """, clean(fullName), clean(department), clean(clubName), userId) > 0;
    }

    public boolean updateMemberPasswordHash(long userId, UserRole role, String passwordHash) {
        if (role != UserRole.STUDENT && role != UserRole.CLUB_REPRESENTATIVE) return false;
        return jdbcTemplate.update("UPDATE campus_users SET password_hash=? WHERE user_id=? AND role=?",
                passwordHash, userId, role.name()) > 0;
    }

    public CampusUser save(String fullName, String email, String passwordHash, UserRole role,
                           String department, Integer yearOfStudy, String rollNumber, String clubName) {
        jdbcTemplate.update("""
                INSERT INTO campus_users (full_name, email, password_hash, role, department,
                    year_of_study, roll_number, club_name, account_status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVE')
                """, fullName.trim(), normalizeEmail(email), passwordHash, role.name(), clean(department),
                yearOfStudy, clean(rollNumber), clean(clubName));
        return findByEmail(email).orElseThrow();
    }

    private static String normalizeEmail(String email) { return email.trim().toLowerCase(Locale.ROOT); }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private static final RowMapper<CampusUser> ROW_MAPPER = UserRepository::mapUser;
    private static CampusUser mapUser(ResultSet rs, int rowNum) throws SQLException {
        Timestamp registered = rs.getTimestamp("registration_date");
        int year = rs.getInt("year_of_study");
        Integer yearOfStudy = rs.wasNull() ? null : year;
        return new CampusUser(rs.getLong("user_id"), rs.getString("full_name"), rs.getString("email"),
                rs.getString("password_hash"), UserRole.valueOf(rs.getString("role")),
                rs.getString("department"), yearOfStudy, rs.getString("roll_number"), rs.getString("club_name"),
                registered == null ? null : registered.toLocalDateTime(),
                AccountStatus.valueOf(rs.getString("account_status")));
    }

    private static AdminUserView mapAdminView(ResultSet rs, int rowNum) throws SQLException {
        Timestamp registered = rs.getTimestamp("registration_date");
        int year = rs.getInt("year_of_study");
        Integer yearOfStudy = rs.wasNull() ? null : year;
        return new AdminUserView(rs.getLong("user_id"), rs.getString("full_name"), rs.getString("email"),
                UserRole.valueOf(rs.getString("role")), rs.getString("department"), yearOfStudy,
                rs.getString("roll_number"), rs.getString("club_name"),
                registered == null ? null : registered.toLocalDateTime(),
                AccountStatus.valueOf(rs.getString("account_status")));
    }
}
