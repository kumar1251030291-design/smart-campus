package com.smartcampus.service;

import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.AdminUserView;
import com.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserManagementService {
    private final UserRepository userRepository;

    public UserManagementService(UserRepository userRepository) { this.userRepository = userRepository; }

    public List<AdminUserView> searchUsers(String search, UserRole role, AccountStatus status) {
        return userRepository.searchUsers(search, role, status);
    }

    public Optional<AdminUserView> findUser(long id) { return userRepository.findAdminViewById(id); }

    public boolean changeStatus(long userId, AccountStatus newStatus, long currentAdminId) {
        if (userId == currentAdminId && newStatus == AccountStatus.DISABLED) {
            throw new SelfDeactivationException("You cannot deactivate your own administrator account.");
        }
        return userRepository.updateAccountStatus(userId, newStatus);
    }

    public static class SelfDeactivationException extends RuntimeException {
        public SelfDeactivationException(String message) { super(message); }
    }
}
