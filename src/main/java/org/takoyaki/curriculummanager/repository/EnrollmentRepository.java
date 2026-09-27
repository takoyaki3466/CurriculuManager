package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.repository.abstracts.AbstractJdbcRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class EnrollmentRepository extends AbstractJdbcRepository<Enrollment> {
    public Enrollment save(Enrollment enrollment) {
        String sql = """
                INSERT INTO enrollments (
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (enrollment.getCurriculumId() == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, enrollment.getCurriculumId());
            }

            statement.setInt(2, enrollment.getCourseId());
            statement.setInt(3, enrollment.getYear());
            statement.setString(4, enrollment.getSemester());

            if (enrollment.getGradeId() == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, enrollment.getGradeId());
            }

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    enrollment.setId(keys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.save"), e);
        }

        return enrollment;
    }

    public Enrollment findById(Integer id) {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapRow(result);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.load"), e);
        }

        return null;
    }

    public List<Enrollment> findAll() {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                ORDER BY
                    year,
                    semester,
                    id
                """;
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                enrollments.add(mapRow(result));
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.list"), e);
        }

        return enrollments;
    }

    public List<Enrollment> findByCurriculumId(Integer curriculumId) {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE curriculum_id = ?
                ORDER BY
                    year,
                    semester,
                    id
                """;
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    enrollments.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.listByCurriculum"), e);
        }

        return enrollments;
    }

    public List<Enrollment> findByCurriculumIdAndYear(Integer curriculumId, int year) {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE curriculum_id = ?
                  AND year = ?
                ORDER BY
                    semester,
                    id
                """;
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);
            statement.setInt(2, year);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    enrollments.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.listByYear"), e);
        }

        return enrollments;
    }

    public List<Enrollment> findByCurriculumIdAndYearAndSemester(Integer curriculumId, int year, String semester) {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE curriculum_id = ?
                  AND year = ?
                  AND semester = ?
                ORDER BY id
                """;
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);
            statement.setInt(2, year);
            statement.setString(3, semester);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    enrollments.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.listBySemester"), e);
        }

        return enrollments;
    }

    public List<Enrollment> findByCourseId(Integer courseId) {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    course_id,
                    year,
                    semester,
                    grade_id
                FROM enrollments
                WHERE course_id = ?
                ORDER BY
                    year,
                    semester,
                    id
                """;
        List<Enrollment> enrollments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, courseId);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    enrollments.add(mapRow(result));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.listByCourse"), e);
        }

        return enrollments;
    }

    public void update(Enrollment enrollment) {
        String sql = """
                UPDATE enrollments
                SET
                    curriculum_id = ?,
                    course_id = ?,
                    year = ?,
                    semester = ?,
                    grade_id = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (enrollment.getCurriculumId() == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, enrollment.getCurriculumId());
            }

            statement.setInt(2, enrollment.getCourseId());
            statement.setInt(3, enrollment.getYear());
            statement.setString(4, enrollment.getSemester());

            if (enrollment.getGradeId() == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, enrollment.getGradeId());
            }

            statement.setInt(6, enrollment.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.update"), e);
        }
    }

    public void deleteById(Integer id) {
        String sql = """
                DELETE FROM enrollments
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.delete"), e);
        }
    }

    public boolean exists(Integer curriculumId, Integer courseId, int year, String semester) {
        String sql = """
                SELECT 1
                FROM enrollments
                WHERE curriculum_id = ?
                  AND course_id = ?
                  AND year = ?
                  AND semester = ?
                LIMIT 1
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);
            statement.setInt(2, courseId);
            statement.setInt(3, year);
            statement.setString(4, semester);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("repository.enrollment.duplicate"), e);
        }
    }

    private Enrollment mapRow(ResultSet result) throws SQLException {
        int curriculumId = result.getInt("curriculum_id");
        Integer nullableCurriculumId = result.wasNull() ? null : curriculumId;
        int gradeId = result.getInt("grade_id");
        Integer nullableGradeId = result.wasNull() ? null : gradeId;
        return new Enrollment(result.getInt("id"), nullableCurriculumId, result.getInt("course_id"), result.getInt("year"), result.getString("semester"), nullableGradeId);
    }
}
