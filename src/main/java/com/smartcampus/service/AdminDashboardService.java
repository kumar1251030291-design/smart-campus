package com.smartcampus.service;

import com.smartcampus.enums.AccountStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.DashboardStats;
import com.smartcampus.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminDashboardService {
    private final ResourceService resourceService;
    private final UserRepository userRepository;

    public AdminDashboardService(ResourceService resourceService, UserRepository userRepository) {
        this.resourceService = resourceService;
        this.userRepository = userRepository;
    }

    public DashboardStats loadStatistics() {
        return new DashboardStats(resourceService.countResources(),
                resourceService.countByCategory(ResourceCategory.CLASSROOM),
                resourceService.countByCategory(ResourceCategory.LABORATORY),
                resourceService.countByCategory(ResourceCategory.EQUIPMENT),
                resourceService.countByCategory(ResourceCategory.FACILITY),
                userRepository.countByRole(UserRole.STUDENT),
                userRepository.countByRole(UserRole.CLUB_REPRESENTATIVE),
                userRepository.countByStatus(AccountStatus.ACTIVE),
                userRepository.countByStatus(AccountStatus.DISABLED));
    }
}
