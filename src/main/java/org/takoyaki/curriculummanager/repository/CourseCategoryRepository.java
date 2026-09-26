package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.repository.abstracts.AbstractJdbcRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CourseCategoryRepository extends AbstractJdbcRepository<CourseCategory> {
    public CourseCategory save(CourseCategory category) throws SQLException {
        String sql = """
                INSERT INTO course_categories (
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, category.getCurriculumId());

            if (category.getParentId() == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, category.getParentId());
            }

            statement.setString(3, category.getName());
            statement.setInt(4, category.getDisplayOrder());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    category.setId(resultSet.getInt(1));
                }
            }
        }

        return category;
    }

    public CourseCategory findById(Integer id) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    public List<CourseCategory> findAll() throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
                ORDER BY
                    curriculum_id,
                    parent_id,
                    display_order,
                    id
                """;
        List<CourseCategory> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                categories.add(mapRow(resultSet));
            }
        }

        return categories;
    }

    public List<CourseCategory> findByCurriculumId(Integer curriculumId) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
                WHERE curriculum_id = ?
                ORDER BY
                    parent_id,
                    display_order,
                    id
                """;
        List<CourseCategory> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(mapRow(resultSet));
                }
            }
        }

        return categories;
    }

    public List<CourseCategory> findByParentId(Integer parentId) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
                WHERE parent_id = ?
                ORDER BY
                    display_order,
                    id
                """;
        List<CourseCategory> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, parentId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(mapRow(resultSet));
                }
            }
        }

        return categories;
    }

    public List<CourseCategory> findRootCategories(Integer curriculumId) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    parent_id,
                    name,
                    display_order
                FROM course_categories
                WHERE curriculum_id = ?
                  AND parent_id IS NULL
                ORDER BY
                    display_order,
                    id
                """;
        List<CourseCategory> categories = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(mapRow(resultSet));
                }
            }
        }

        return categories;
    }

    public void update(CourseCategory category) throws SQLException {
        String sql = """
                UPDATE course_categories
                SET
                    curriculum_id = ?,
                    parent_id = ?,
                    name = ?,
                    display_order = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, category.getCurriculumId());

            if (category.getParentId() == null) {
                statement.setNull(2, java.sql.Types.INTEGER);
            } else {
                statement.setInt(2, category.getParentId());
            }

            statement.setString(3, category.getName());
            statement.setInt(4, category.getDisplayOrder());
            statement.setInt(5, category.getId());
            statement.executeUpdate();
        }
    }

    public void deleteById(Integer id) throws SQLException {
        String sql = """
                DELETE FROM course_categories
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private CourseCategory mapRow(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        Integer curriculumId = resultSet.getInt("curriculum_id");
        int parentIdValue = resultSet.getInt("parent_id");
        Integer parentId;

        if (resultSet.wasNull()) {
            parentId = null;
        } else {
            parentId = parentIdValue;
        }

        String name = resultSet.getString("name");
        int displayOrder = resultSet.getInt("display_order");
        return new CourseCategory(id, curriculumId, parentId, name, displayOrder);
    }
}
