package com.smartcampus.config;

import com.smartcampus.service.CampusUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


@Configuration
public class SecurityConfig {

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, CampusUserDetailsService userDetailsService,
                                            com.smartcampus.repository.UserRepository userRepository,
                                            AuthenticationSuccessHandler loginSuccessHandler,
                                            AuthenticationFailureHandler loginFailureHandler) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/resources", "/resources/**", "/login", "/login/admin", "/register",
                                "/css/**", "/js/**", "/images/**", "/announcements", "/announcements/**", "/error", "/h2-console/**").permitAll()
                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/student", "/student/**").hasRole("STUDENT")
                        .requestMatchers("/club", "/club/**").hasRole("CLUB_REPRESENTATIVE")
                        .requestMatchers("/notifications", "/notifications/**").hasAnyRole("STUDENT", "CLUB_REPRESENTATIVE")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login/authenticate")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .successHandler(loginSuccessHandler)
                        .failureHandler(loginFailureHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        http.addFilterBefore(new ActiveAccountFilter(userRepository), AuthorizationFilter.class);
        return http.build();
    }

    @Bean
    AuthenticationSuccessHandler loginSuccessHandler() {
        return (request, response, authentication) -> {
            boolean adminLogin = "ADMIN".equalsIgnoreCase(request.getParameter("loginMode"));
            boolean isAdmin = hasRole(authentication, "ADMIN");
            if (adminLogin != isAdmin) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) request.getSession(false).invalidate();
                response.sendRedirect(adminLogin ? "/login/admin?roleError" : "/login?roleError");
                return;
            }

            String destination;
            if (isAdmin) {
                destination = "/admin/dashboard";
            } else if (hasRole(authentication, "STUDENT")) {
                destination = "/student/dashboard";
            } else if (hasRole(authentication, "CLUB_REPRESENTATIVE")) {
                destination = "/club/dashboard";
            } else {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) request.getSession(false).invalidate();
                response.sendRedirect("/login?roleError");
                return;
            }
            response.sendRedirect(request.getContextPath() + destination);
        };
    }

    private static boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Bean
    AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            boolean adminLogin = "ADMIN".equalsIgnoreCase(request.getParameter("loginMode"));
            response.sendRedirect(adminLogin ? "/login/admin?error" : "/login?error");
        };
    }
}
