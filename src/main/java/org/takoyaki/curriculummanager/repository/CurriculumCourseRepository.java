package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.CurriculumCourse;
import org.takoyaki.curriculummanager.repository.abstracts.AbstractJdbcRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CurriculumCourseRepository extends AbstractJdbcRepository<CurriculumCourse> {
    public CurriculumCourse save(CurriculumCourse curriculumCourse) throws SQLException {
        String sql = """
                INSERT INTO curriculum_courses (
                    curriculum_id,
                    course_id,
                    requirement_type
                )
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, curriculumCourse.getCurriculumId());
            statement.setInt(2, curriculumCourse.getCourseId());
            statement.setString(3, curriculumCourse.getRequirementType());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    curriculumCourse.setId(resultSet.getInt(1));
                }
            }
        }

        return curriculumCourse;
    }

    public CurriculumCourse findById(Integer id) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
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

    public List<CurriculumCourse> findAll() throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                ORDER BY
                    curriculum_id,
                    course_id
                """;
        List<CurriculumCourse> curriculumCourses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                curriculumCourses.add(mapRow(resultSet));
            }
        }

        return curriculumCourses;
    }

    public List<CurriculumCourse> findByCurriculumId(Integer curriculumId) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                WHERE curriculum_id = ?
                ORDER BY course_id
                """;
        List<CurriculumCourse> curriculumCourses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    curriculumCourses.add(mapRow(resultSet));
                }
            }
        }

        return curriculumCourses;
    }

    public List<CurriculumCourse> findByCourseId(Integer courseId) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                WHERE course_id = ?
                ORDER BY curriculum_id
                """;
        List<CurriculumCourse> curriculumCourses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, courseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    curriculumCourses.add(mapRow(resultSet));
                }
            }
        }

        return curriculumCourses;
    }

    public CurriculumCourse findByCurriculumIdAndCourseId(Integer curriculumId, Integer courseId) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                WHERE curriculum_id = ?
                  AND course_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);
            statement.setInt(2, courseId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    public List<CurriculumCourse> findByCurriculumIdAndRequirementType(Integer curriculumId, String requirementType) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    requirement_type
                FROM curriculum_courses
                WHERE curriculum_id = ?
                  AND requirement_type = ?
                ORDER BY course_id
                """;
        List<CurriculumCourse> curriculumCourses = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);
            statement.setString(2, requirementType);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    curriculumCourses.add(mapRow(resultSet));
                }
            }
        }

        return curriculumCourses;
    }

    public void update(CurriculumCourse curriculumCourse) throws SQLException {
        String sql = """
                UPDATE curriculum_courses
                SET
                    curriculum_id = ?,
                    course_id = ?,
                    requirement_type = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumCourse.getCurriculumId());
            statement.setInt(2, curriculumCourse.getCourseId());
            statement.setString(3, curriculumCourse.getRequirementType());
            statement.setInt(4, curriculumCourse.getId());
            statement.executeUpdate();
        }
    }

    public void deleteById(Integer id) throws SQLException {
        String sql = """
                DELETE FROM curriculum_courses
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public void deleteByCurriculumIdAndCourseId(Integer curriculumId, Integer courseId) throws SQLException {
        String sql = """
                DELETE FROM curriculum_courses
                WHERE curriculum_id = ?
                  AND course_id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);
            statement.setInt(2, courseId);
            statement.executeUpdate();
        }
    }

    private CurriculumCourse mapRow(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        Integer curriculumId = resultSet.getInt("curriculum_id");
        Integer courseId = resultSet.getInt("course_id");
        String requirementType = resultSet.getString("requirement_type");
        return new CurriculumCourse(id, curriculumId, courseId, requirementType);
    }
}
