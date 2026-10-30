package com.smartcampus.service;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.model.Resource;
import com.smartcampus.model.ResourceForm;
import com.smartcampus.model.Classroom;
import com.smartcampus.model.Laboratory;
import com.smartcampus.model.Equipment;
import com.smartcampus.model.Facility;
import com.smartcampus.repository.ResourceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Locale;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;

    public ResourceService(ResourceRepository resourceRepository) {
        this.resourceRepository = resourceRepository;
    }

    public List<Resource> findAllResources() {
        return resourceRepository.findAll();
    }

    public List<Resource> searchResources(String searchText, ResourceCategory category, AvailabilityStatus status) {
        String cleanedSearchText = searchText == null ? "" : searchText.trim();
        return resourceRepository.search(cleanedSearchText, category, status);
    }

    public List<Resource> searchResources(String searchText, ResourceCategory category, AvailabilityStatus status,
                                          Integer minimumCapacity, Integer maximumCapacity) {
        String cleanedSearchText = searchText == null ? "" : searchText.trim();
        return resourceRepository.search(cleanedSearchText, category, status, minimumCapacity, maximumCapacity);
    }

    public Optional<Resource> findResource(long id) { return resourceRepository.findById(id); }

    public long countResources() { return resourceRepository.countAll(); }

    public long countByCategory(ResourceCategory category) { return resourceRepository.countByCategory(category); }

    public Resource createResource(ResourceForm form) {
        Resource resource = toResource(resourceRepository.nextId(), form);
        resourceRepository.insert(resource);
        return resource;
    }

    public boolean updateResource(long id, ResourceForm form) {
        return resourceRepository.update(toResource(id, form));
    }

    public boolean updateAvailability(long id, AvailabilityStatus status) {
        return resourceRepository.findById(id).map(resource -> {
            resource.setAvailabilityStatus(status);
            return resourceRepository.update(resource);
        }).orElse(false);
    }

    public boolean deleteResource(long id) { return resourceRepository.deleteById(id); }

    private Resource toResource(long id, ResourceForm form) {
        String name = form.getName().trim();
        String description = form.getDescription().trim();
        String location = form.getLocation().trim();
        ResourceCategory category = ResourceCategory.valueOf(form.getCategory().trim().toUpperCase(Locale.ROOT));
        AvailabilityStatus status = AvailabilityStatus.valueOf(form.getAvailabilityStatus().trim().toUpperCase(Locale.ROOT));
        return switch (category) {
            case CLASSROOM -> new Classroom(id, name, description, location, form.getCapacityOrQuantity(), status);
            case LABORATORY -> new Laboratory(id, name, description, location, form.getCapacityOrQuantity(), status);
            case EQUIPMENT -> new Equipment(id, name, description, location, form.getCapacityOrQuantity(), status);
            case FACILITY -> new Facility(id, name, description, location, form.getCapacityOrQuantity(), status);
        };
    }
}
