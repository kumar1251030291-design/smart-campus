package com.smartcampus.repository;

import com.smartcampus.enums.AvailabilityStatus;
import com.smartcampus.enums.ResourceCategory;
import com.smartcampus.model.Classroom;
import com.smartcampus.model.Equipment;
import com.smartcampus.model.Facility;
import com.smartcampus.model.Laboratory;
import com.smartcampus.model.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Repository
public class ResourceRepository {

    private static final String BASE_QUERY = """
            SELECT id, name, description, location, capacity_or_quantity, category, availability_status
            FROM campus_resources
            WHERE 1 = 1
            """;
    private static final String CATEGORY_ORDER = """
             ORDER BY CASE category
                 WHEN 'CLASSROOM' THEN 1
                 WHEN 'LABORATORY' THEN 2
                 WHEN 'EQUIPMENT' THEN 3
                 WHEN 'FACILITY' THEN 4
             END, name
            """;

    private final JdbcTemplate jdbcTemplate;

    public ResourceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Resource> findAll() {
        return jdbcTemplate.query(BASE_QUERY + CATEGORY_ORDER, this::mapResource);
    }

    public Optional<Resource> findById(long id) {
        return jdbcTemplate.query(BASE_QUERY + " AND id = ?", this::mapResource, id)
                .stream().findFirst();
    }

    public long countAll() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM campus_resources", Long.class);
        return count == null ? 0 : count;
    }

    public long countByCategory(ResourceCategory category) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM campus_resources WHERE category = ?", Long.class, category.name());
        return count == null ? 0 : count;
    }

    public long nextId() {
        Long id = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM campus_resources", Long.class);
        return id == null ? 1 : id;
    }

    public void insert(Resource resource) {
        jdbcTemplate.update("""
                INSERT INTO campus_resources (id, name, description, location, capacity_or_quantity, category, availability_status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, resource.getId(), resource.getName(), resource.getDescription(), resource.getLocation(),
                resource.getCapacityOrQuantity(), resource.getCategory().name(), resource.getAvailabilityStatus().name());
    }

    public boolean update(Resource resource) {
        return jdbcTemplate.update("""
                UPDATE campus_resources
                SET name = ?, description = ?, location = ?, capacity_or_quantity = ?, category = ?, availability_status = ?
                WHERE id = ?
                """, resource.getName(), resource.getDescription(), resource.getLocation(),
                resource.getCapacityOrQuantity(), resource.getCategory().name(),
                resource.getAvailabilityStatus().name(), resource.getId()) > 0;
    }

    public boolean deleteById(long id) {
        return jdbcTemplate.update("DELETE FROM campus_resources WHERE id = ?", id) > 0;
    }

    public List<Resource> search(String searchText, ResourceCategory category, AvailabilityStatus status) {
        return search(searchText, category, status, null, null);
    }

    public List<Resource> search(String searchText, ResourceCategory category, AvailabilityStatus status,
                                 Integer minimumCapacity, Integer maximumCapacity) {
        StringBuilder sql = new StringBuilder(BASE_QUERY);
        List<Object> parameters = new ArrayList<>();

        if (searchText != null && !searchText.isBlank()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ?)");
            String search = "%" + searchText.trim().toLowerCase(Locale.ROOT) + "%";
            parameters.add(search);
            parameters.add(search);
        }
        if (category != null) {
            sql.append(" AND category = ?");
            parameters.add(category.name());
        }
        if (status != null) {
            sql.append(" AND availability_status = ?");
            parameters.add(status.name());
        }
        if (minimumCapacity != null) { sql.append(" AND capacity_or_quantity >= ?"); parameters.add(minimumCapacity); }
        if (maximumCapacity != null) { sql.append(" AND capacity_or_quantity <= ?"); parameters.add(maximumCapacity); }

        sql.append(CATEGORY_ORDER);
        return jdbcTemplate.query(sql.toString(), this::mapResource, parameters.toArray());
    }

    private Resource mapResource(ResultSet resultSet, int rowNumber) throws SQLException {
        Long id = resultSet.getLong("id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        String location = resultSet.getString("location");
        int capacityOrQuantity = resultSet.getInt("capacity_or_quantity");
        ResourceCategory category = ResourceCategory.valueOf(resultSet.getString("category"));
        AvailabilityStatus status = AvailabilityStatus.valueOf(resultSet.getString("availability_status"));

        return switch (category) {
            case CLASSROOM -> new Classroom(id, name, description, location, capacityOrQuantity, status);
            case LABORATORY -> new Laboratory(id, name, description, location, capacityOrQuantity, status);
            case EQUIPMENT -> new Equipment(id, name, description, location, capacityOrQuantity, status);
            case FACILITY -> new Facility(id, name, description, location, capacityOrQuantity, status);
        };
    }
}
