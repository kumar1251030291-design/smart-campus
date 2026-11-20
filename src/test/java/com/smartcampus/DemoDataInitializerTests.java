package com.smartcampus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:smartcampus-demo-seed;DB_CLOSE_DELAY=-1",
        "smartcampus.demo.enabled=true",
        "smartcampus.admin.email=demo-admin@college.edu",
        "smartcampus.admin.password=demo-admin-test-only"})
class DemoDataInitializerTests {
    @Autowired private JdbcTemplate jdbc;

    @Test
    void demoSeederCreatesAccountsBookingsAndDecisionsFromAnEmptyDatabase() {
        assertThat(count("SELECT COUNT(*) FROM campus_users WHERE role <> 'ADMIN'")).isEqualTo(4);
        assertThat(count("SELECT COUNT(*) FROM booking_requests")).isEqualTo(20);
        assertThat(count("SELECT COUNT(*) FROM booking_requests WHERE status='APPROVED'")).isEqualTo(8);
        assertThat(count("SELECT COUNT(*) FROM booking_requests WHERE status='REJECTED'")).isEqualTo(4);
        assertThat(count("SELECT COUNT(*) FROM booking_requests WHERE decision_note IS NOT NULL")).isGreaterThanOrEqualTo(15);
        assertThat(count("SELECT COUNT(*) FROM portal_notifications")).isGreaterThan(20);
        assertThat(count("SELECT COUNT(*) FROM equipment_allocations WHERE equipment_status='ISSUED'")).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM resource_views")).isEqualTo(17);
    }

    private int count(String sql) { return jdbc.queryForObject(sql, Integer.class); }
}
