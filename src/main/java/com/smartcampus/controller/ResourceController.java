package com.smartcampus.controller;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.enums.ResourceDiscoveryGroup;
import com.smartcampus.model.Resource;
import com.smartcampus.enums.UserRole;
import com.smartcampus.model.CampusUser;
import com.smartcampus.repository.EquipmentRepository;
import com.smartcampus.repository.ResourceViewRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.smartcampus.service.ResourceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResourceController {

    private final ResourceService resourceService;
    private final EquipmentRepository equipmentRepository;
    private final ResourceViewRepository resourceViews;

    public ResourceController(ResourceService resourceService, EquipmentRepository equipmentRepository,
                              ResourceViewRepository resourceViews) {
        this.resourceService = resourceService;
        this.equipmentRepository = equipmentRepository;
        this.resourceViews = resourceViews;
    }

    @GetMapping("/resources")
    public String showResources(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) ResourceCategory category,
            @RequestParam(name = "availability", required = false) AvailabilityStatus availability,
            @RequestParam(required = false) ResourceDiscoveryGroup group,
            @RequestParam(required = false) Integer minimumCapacity,
            @RequestParam(required = false) Integer maximumCapacity,
            Model model) {
        var resources = resourceService.searchResources(search, category, availability,
                validBound(minimumCapacity), validBound(maximumCapacity));
        if (group != null) resources = resources.stream().filter(group::matches).toList();
        model.addAttribute("resources", resources);
        model.addAttribute("categories", ResourceCategory.values());
        model.addAttribute("statuses", AvailabilityStatus.values());
        model.addAttribute("groups", ResourceDiscoveryGroup.values());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", category == null ? "" : category.name());
        model.addAttribute("selectedAvailability", availability == null ? "" : availability.name());
        model.addAttribute("selectedGroup", group == null ? "" : group.name());
        model.addAttribute("minimumCapacity", minimumCapacity);
        model.addAttribute("maximumCapacity", maximumCapacity);
        model.addAttribute("filtersApplied", !search.isBlank() || category != null || availability != null || group != null
                || minimumCapacity != null || maximumCapacity != null);
        return "resources";
    }

    @GetMapping("/resources/{id}")
    public String details(@PathVariable long id, @AuthenticationPrincipal CampusUser user, Model model) {
        return resourceService.findResource(id).map(resource -> {
            if (user != null && (user.getRole() == UserRole.STUDENT || user.getRole() == UserRole.CLUB_REPRESENTATIVE)) {
                resourceViews.record(user.getUserId(), id);
            }
            model.addAttribute("resource", resource);
            model.addAttribute("resourceType", resource.getClass().getSimpleName());
            model.addAttribute("discoveryGroups", java.util.Arrays.stream(ResourceDiscoveryGroup.values())
                    .filter(group -> group.matches(resource)).map(ResourceDiscoveryGroup::getDisplayName).toList());
            model.addAttribute("assets", equipmentRepository.findAssetsForResource(id));
            return "resource-details";
        }).orElse("redirect:/resources?error=not-found");
    }

    private Integer validBound(Integer value) { return value != null && value >= 0 ? value : null; }
}
