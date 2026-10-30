package com.smartcampus.config;

import com.smartcampus.enums.AccountStatus;
import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Rechecks persisted account state so deactivation also ends an existing signed-in session. */
public class ActiveAccountFilter extends OncePerRequestFilter {
    private final UserRepository userRepository;

    public ActiveAccountFilter(UserRepository userRepository) { this.userRepository = userRepository; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CampusUser signedInUser) {
            boolean stillActive = userRepository.findByEmail(signedInUser.getEmail())
                    .map(user -> user.getAccountStatus() == AccountStatus.ACTIVE)
                    .orElse(false);
            if (!stillActive) {
                SecurityContextHolder.clearContext();
                if (request.getSession(false) != null) request.getSession(false).invalidate();
                response.sendRedirect(request.getContextPath() + "/login?disabled");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
