package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.repository.abstracts.AbstractJdbcRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CourseRepository extends AbstractJdbcRepository<Course> {
    public Course save(Course course) throws SQLException {
        String sql = """
                INSERT INTO courses (
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (course.getCourseCode() == null) {
                statement.setNull(1, java.sql.Types.VARCHAR);
            } else {
                statement.setString(1, course.getCourseCode());
            }

            statement.setString(2, course.getName());
            statement.setDouble(3, course.getCredits());

            if (course.getCategoryId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, course.getCategoryId());
            }

            if (course.getDescription() == null) {
                statement.setNull(5, java.sql.Types.VARCHAR);
            } else {
                statement.setString(5, course.getDescription());
            }

            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    course.setId(resultSet.getInt(1));
                }
            }
        }

        return course;
    }

    public Course findById(Integer id) throws SQLException {
        String sql = """
                SELECT
                    id,
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                FROM courses
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

    public List<Course> findAll() throws SQLException {
        String sql = """
                SELECT
                    id,
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                FROM courses
                ORDER BY name, id
                """;
        List<Course> courses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                courses.add(mapRow(resultSet));
            }
        }

        return courses;
    }

    public Course findByCourseCode(String courseCode) throws SQLException {
        String sql = """
                SELECT
                    id,
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                FROM courses
                WHERE course_code = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseCode);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    public List<Course> findByCategoryId(Integer categoryId) throws SQLException {
        String sql = """
                SELECT
                    id,
                    course_code,
                    name,
                    credits,
                    category_id,
                    description
                FROM courses
                WHERE category_id = ?
                ORDER BY name, id
                """;
        List<Course> courses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    courses.add(mapRow(resultSet));
                }
            }
        }

        return courses;
    }

    public void update(Course course) throws SQLException {
        String sql = """
                UPDATE courses
                SET
                    course_code = ?,
                    name = ?,
                    credits = ?,
                    category_id = ?,
                    description = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (course.getCourseCode() == null) {
                statement.setNull(1, java.sql.Types.VARCHAR);
            } else {
                statement.setString(1, course.getCourseCode());
            }

            statement.setString(2, course.getName());
            statement.setDouble(3, course.getCredits());

            if (course.getCategoryId() == null) {
                statement.setNull(4, java.sql.Types.INTEGER);
            } else {
                statement.setInt(4, course.getCategoryId());
            }

            if (course.getDescription() == null) {
                statement.setNull(5, java.sql.Types.VARCHAR);
            } else {
                statement.setString(5, course.getDescription());
            }

            statement.setInt(6, course.getId());
            statement.executeUpdate();
        }
    }

    public void deleteById(Integer id) throws SQLException {
        String sql = """
                DELETE FROM courses
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Course mapRow(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        String courseCode = resultSet.getString("course_code");
        String name = resultSet.getString("name");
        double credits = resultSet.getDouble("credits");
        int categoryIdValue = resultSet.getInt("category_id");
        Integer categoryId;

        if (resultSet.wasNull()) {
            categoryId = null;
        } else {
            categoryId = categoryIdValue;
        }

        String description = resultSet.getString("description");
        return new Course(id, courseCode, name, credits, categoryId, description);
    }
}
