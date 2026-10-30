package com.smartcampus.config;

import com.smartcampus.enums.UserRole;
import com.smartcampus.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;

@Configuration
public class AdminAccountInitializer {

    @Bean
    CommandLineRunner initializeAdminAccount(UserRepository users, PasswordEncoder passwordEncoder,
            @Value("${smartcampus.admin.email:}") String configuredEmail,
            @Value("${smartcampus.admin.password:}") String configuredPassword) {
        return args -> {
            if (configuredEmail.isBlank() && configuredPassword.isBlank()) return;
            if (configuredEmail.isBlank() || configuredPassword.isBlank()) {
                throw new IllegalStateException("Set both SMART_CAMPUS_ADMIN_EMAIL and SMART_CAMPUS_ADMIN_PASSWORD.");
            }
            if (configuredPassword.length() < 6 || configuredPassword.length() > 72
                    || configuredPassword.getBytes(StandardCharsets.UTF_8).length > 72) {
                throw new IllegalStateException("The configured demo admin password must be 6 to 72 characters and no more than 72 UTF-8 bytes.");
            }
            String email = configuredEmail.trim().toLowerCase(Locale.ROOT);
            if (!Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", email)) {
                throw new IllegalStateException("SMART_CAMPUS_ADMIN_EMAIL must be a valid email address.");
            }
            if (users.existsByEmail(email)) {
                if (users.findByEmail(email).orElseThrow().getRole() != UserRole.ADMIN) {
                    throw new IllegalStateException("The configured admin email already belongs to a non-admin account.");
                }
                return;
            }
            users.save("Campus Administrator", email, passwordEncoder.encode(configuredPassword), UserRole.ADMIN,
                    null, null, null, null);
        };
    }
}
