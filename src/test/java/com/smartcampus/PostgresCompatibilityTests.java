package com.smartcampus;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the real schema, seed data, demo bookings and the main pages against PostgreSQL
 * (the database used by the hosted site), so H2-only SQL cannot slip into production.
 */
@SpringBootTest(properties = {"smartcampus.demo.enabled=true", "spring.h2.console.enabled=false"})
@AutoConfigureMockMvc
class PostgresCompatibilityTests {
    private static final EmbeddedPostgres POSTGRES;
    static {
        try { POSTGRES = EmbeddedPostgres.start(); }
        catch (IOException exception) { throw new IllegalStateException(exception); }
    }

    @DynamicPropertySource
    static void postgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> POSTGRES.getJdbcUrl("postgres", "postgres"));
        registry.add("spring.datasource.username", () -> "postgres");
        registry.add("spring.datasource.password", () -> "postgres");
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @AfterAll
    static void stop() throws IOException { POSTGRES.close(); }

    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mockMvc;
    @Autowired private com.smartcampus.repository.UserRepository users;

    @Test
    void schemaSeedsDemoBookingsAndPagesWorkOnPostgres() throws Exception {
        assertThat(count("SELECT COUNT(*) FROM campus_resources")).isEqualTo(85);
        assertThat(count("SELECT COUNT(*) FROM campus_announcements")).isGreaterThanOrEqualTo(6);
        assertThat(count("SELECT COUNT(*) FROM booking_requests")).isEqualTo(20);
        assertThat(count("SELECT COUNT(*) FROM booking_requests WHERE status='APPROVED'")).isEqualTo(8);
        assertThat(count("SELECT COUNT(*) FROM portal_notifications")).isGreaterThan(20);
        // The last seeding steps (member responses, equipment issue, recent views) must also have succeeded.
        assertThat(count("SELECT COUNT(*) FROM booking_requests WHERE status='UNDER_REVIEW'")).isEqualTo(3);
        assertThat(count("SELECT COUNT(*) FROM equipment_allocations WHERE equipment_status='ISSUED'")).isEqualTo(1);
        assertThat(count("SELECT COUNT(*) FROM resource_views")).isEqualTo(17);

        var student = users.findByEmail("student1@gmail.com").orElseThrow();
        var club = users.findByEmail("club1@gmail.com").orElseThrow();
        var admin = users.findById(users.findActiveAdminIds().getFirst()).orElseThrow();
        long bookingId = jdbc.queryForObject("SELECT MIN(booking_id) FROM booking_requests WHERE requester_id=?", Long.class, student.getUserId());

        mockMvc.perform(get("/resources?search=lab")).andExpect(status().isOk());
        mockMvc.perform(get("/announcements")).andExpect(status().isOk());
        mockMvc.perform(get("/student/dashboard").with(user(student))).andExpect(status().isOk());
        mockMvc.perform(get("/student/bookings").with(user(student))).andExpect(status().isOk());
        mockMvc.perform(get("/student/bookings/" + bookingId).with(user(student))).andExpect(status().isOk());
        mockMvc.perform(get("/student/bookings/" + bookingId + "/decision.pdf").with(user(student))).andExpect(status().isOk());
        mockMvc.perform(get("/notifications").with(user(student))).andExpect(status().isOk());
        mockMvc.perform(get("/club/dashboard").with(user(club))).andExpect(status().isOk());
        mockMvc.perform(get("/admin/dashboard").with(user(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/admin/bookings").with(user(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/admin/bookings/" + bookingId).with(user(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/admin/equipment").with(user(admin))).andExpect(status().isOk());
        mockMvc.perform(get("/admin/users").with(user(admin))).andExpect(status().isOk());
    }

    private int count(String sql) { return jdbc.queryForObject(sql, Integer.class); }
}
