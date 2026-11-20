package com.smartcampus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:smartcampus-seed-safety;DB_CLOSE_DELAY=-1")
class DatabaseSeedSafetyTests {
    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void sampleSeedsAreInsertedOnceAndNeverOverwriteExistingCampusData() {
        // Spring Boot has initialized this dedicated in-memory database from empty.
        assertThat(count("campus_resources")).isEqualTo(85);
        assertThat(count("equipment_assets")).isEqualTo(16);

        initializeSchemaAndSeeds();

        assertThat(count("campus_resources")).isEqualTo(85);
        assertThat(count("equipment_assets")).isEqualTo(16);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM campus_resources WHERE id=1", Integer.class)).isEqualTo(1);
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM equipment_assets WHERE asset_tag='ARD-001'", Integer.class)).isEqualTo(1);

        jdbc.update("UPDATE campus_resources SET description=?, availability_status=? WHERE id=?",
                "Administrator-edited description", "MAINTENANCE", 38L);
        jdbc.update("UPDATE equipment_assets SET asset_status=?, notes=? WHERE asset_tag=?",
                "ISSUED", "Issued to a student", "ARD-001");

        insertExistingBookingAndAllocation();
        initializeSchemaAndSeeds();

        assertThat(jdbc.queryForObject("SELECT description FROM campus_resources WHERE id=38", String.class))
                .isEqualTo("Administrator-edited description");
        assertThat(jdbc.queryForObject("SELECT availability_status FROM campus_resources WHERE id=38", String.class))
                .isEqualTo("MAINTENANCE");
        assertThat(jdbc.queryForObject("SELECT asset_status FROM equipment_assets WHERE asset_tag='ARD-001'", String.class))
                .isEqualTo("ISSUED");
        assertThat(jdbc.queryForObject("SELECT notes FROM equipment_assets WHERE asset_tag='ARD-001'", String.class))
                .isEqualTo("Issued to a student");

        assertThat(jdbc.queryForObject("SELECT full_name FROM campus_users WHERE user_id=90001", String.class))
                .isEqualTo("Seed Test Student");
        assertThat(jdbc.queryForObject("SELECT event_title FROM booking_requests WHERE booking_id=90001", String.class))
                .isEqualTo("Existing test booking");
        assertThat(jdbc.queryForObject("SELECT status FROM booking_requests WHERE booking_id=90001", String.class))
                .isEqualTo("APPROVED");
        assertThat(jdbc.queryForObject("SELECT note FROM booking_history WHERE history_id=90001", String.class))
                .isEqualTo("Existing history entry");
        assertThat(jdbc.queryForObject("SELECT asset_tag FROM equipment_allocations WHERE allocation_id=90001", String.class))
                .isEqualTo("ARD-001");
        assertThat(jdbc.queryForObject("SELECT equipment_status FROM equipment_allocations WHERE allocation_id=90001", String.class))
                .isEqualTo("ISSUED");
        assertThat(count("campus_resources")).isEqualTo(85);
        assertThat(count("equipment_assets")).isEqualTo(16);
    }

    private void initializeSchemaAndSeeds() {
        new ResourceDatabasePopulator(new ClassPathResource("schema.sql")).execute(dataSource);
        new ResourceDatabasePopulator(new ClassPathResource("data.sql")).execute(dataSource);
    }

    private int count(String tableName) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class);
    }

    private void insertExistingBookingAndAllocation() {
        jdbc.update("""
                INSERT INTO campus_users (user_id, full_name, email, password_hash, role, department)
                VALUES (90001, 'Seed Test Student', 'seed-test@college.edu', 'not-a-real-password-hash', 'STUDENT', 'Engineering')
                """);
        jdbc.update("""
                INSERT INTO booking_requests (booking_id, booking_code, requester_id, requester_name, requester_role,
                    department, event_title, event_type, purpose, event_date, start_time, end_time,
                    expected_participants, status)
                VALUES (90001, 'SC-SEED-TEST', 90001, 'Seed Test Student', 'STUDENT', 'Engineering',
                    'Existing test booking', 'ACADEMIC', 'Verify seed scripts preserve this booking.',
                    DATE '2030-11-01', TIME '10:00:00', TIME '11:00:00', 10, 'APPROVED')
                """);
        jdbc.update("""
                INSERT INTO booking_request_items (item_id, booking_id, resource_id, requested_quantity)
                VALUES (90001, 90001, 38, 1)
                """);
        jdbc.update("""
                INSERT INTO booking_history (history_id, booking_id, status_after, action, note, changed_by)
                VALUES (90001, 90001, 'APPROVED', 'TEST_APPROVED', 'Existing history entry', 90001)
                """);
        jdbc.update("""
                INSERT INTO equipment_allocations (allocation_id, booking_id, item_id, resource_id, asset_tag,
                    allocated_quantity, assigned_user_id, issued_by, equipment_status)
                VALUES (90001, 90001, 90001, 38, 'ARD-001', 1, 90001, 90001, 'ISSUED')
                """);
    }
}
