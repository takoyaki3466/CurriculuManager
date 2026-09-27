package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.GraduationRequirement;
import org.takoyaki.curriculummanager.repository.abstracts.AbstractJdbcRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.takoyaki.curriculummanager.util.CollectionUtils.uniqueNonNull;
import static org.takoyaki.curriculummanager.util.TransactionUtils.execute;

public class GraduationRequirementRepository extends AbstractJdbcRepository<GraduationRequirement> {
    public GraduationRequirement save(GraduationRequirement requirement) throws SQLException {
        String sql = """
                INSERT INTO graduation_requirements (
                    curriculum_id,
                    category_id,
                    name,
                    required_credits
                )
                VALUES (?, ?, ?, ?)
                """;

        return execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, requirement.getCurriculumId());
                setLegacyCategoryId(statement, 2, requirement);
                statement.setString(3, requirement.getName());
                statement.setDouble(4, requirement.getRequiredCredits());
                statement.executeUpdate();

                try (ResultSet resultSet = statement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        requirement.setId(resultSet.getInt(1));
                    } else {
                        throw new SQLException(I18n.raw("repository.graduation.idMissing"));
                    }
                }
            }

            insertCategoryRelations(connection, requirement);
            return requirement;
        });
    }

    public GraduationRequirement findById(Integer id) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    name,
                    required_credits
                FROM graduation_requirements
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(connection, resultSet);
                }
            }
        }

        return null;
    }

    public List<GraduationRequirement> findAll() throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    name,
                    required_credits
                FROM graduation_requirements
                ORDER BY
                    curriculum_id,
                    id
                """;
        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                requirements.add(mapRow(connection, resultSet));
            }
        }

        return requirements;
    }

    public List<GraduationRequirement> findByCurriculumId(Integer curriculumId) throws SQLException {
        String sql = """
                SELECT
                    id,
                    curriculum_id,
                    name,
                    required_credits
                FROM graduation_requirements
                WHERE curriculum_id = ?
                ORDER BY id
                """;
        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requirements.add(mapRow(connection, resultSet));
                }
            }
        }

        return requirements;
    }

    public List<GraduationRequirement> findByCategoryId(Integer categoryId) throws SQLException {
        String sql = """
                SELECT DISTINCT
                    gr.id,
                    gr.curriculum_id,
                    gr.name,
                    gr.required_credits
                FROM graduation_requirements gr
                INNER JOIN graduation_requirement_categories grc
                    ON gr.id = grc.requirement_id
                WHERE grc.category_id = ?
                ORDER BY gr.id
                """;
        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requirements.add(mapRow(connection, resultSet));
                }
            }
        }

        return requirements;
    }

    public List<GraduationRequirement> findOverallRequirements(Integer curriculumId) throws SQLException {
        String sql = """
                SELECT
                    gr.id,
                    gr.curriculum_id,
                    gr.name,
                    gr.required_credits
                FROM graduation_requirements gr
                WHERE gr.curriculum_id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM graduation_requirement_categories grc
                      WHERE grc.requirement_id = gr.id
                  )
                ORDER BY gr.id
                """;
        List<GraduationRequirement> requirements = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, curriculumId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requirements.add(mapRow(connection, resultSet));
                }
            }
        }

        return requirements;
    }

    public void update(GraduationRequirement requirement) throws SQLException {
        if (requirement.getId() == null) {
            throw new IllegalArgumentException(I18n.raw("repository.graduation.updateIdRequired"));
        }

        String sql = """
                UPDATE graduation_requirements
                SET
                    curriculum_id = ?,
                    category_id = ?,
                    name = ?,
                    required_credits = ?
                WHERE id = ?
                """;

        execute(connection -> {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, requirement.getCurriculumId());
                setLegacyCategoryId(statement, 2, requirement);
                statement.setString(3, requirement.getName());
                statement.setDouble(4, requirement.getRequiredCredits());
                statement.setInt(5, requirement.getId());
                int updated = statement.executeUpdate();

                if (updated == 0) {
                    throw new SQLException(I18n.raw("repository.graduation.updateMissing", requirement.getId()));
                }
            }

            deleteCategoryRelations(connection, requirement.getId());
            insertCategoryRelations(connection, requirement);
            return null;
        });
    }

    public void deleteById(Integer id) throws SQLException {
        String sql = """
                DELETE FROM graduation_requirements
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void insertCategoryRelations(Connection connection, GraduationRequirement requirement) throws SQLException {
        List<Integer> categoryIds = uniqueNonNull(requirement.getCategoryIds());

        if (categoryIds.isEmpty()) {
            return;
        }

        String sql = """
                INSERT OR IGNORE INTO
                graduation_requirement_categories (
                    requirement_id,
                    category_id
                )
                VALUES (?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Integer categoryId : categoryIds) {
                statement.setInt(1, requirement.getId());
                statement.setInt(2, categoryId);
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void deleteCategoryRelations(Connection connection, Integer requirementId) throws SQLException {
        String sql = """
                DELETE FROM graduation_requirement_categories
                WHERE requirement_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requirementId);
            statement.executeUpdate();
        }
    }

    private List<Integer> findCategoryIds(Connection connection, Integer requirementId) throws SQLException {
        String sql = """
                SELECT category_id
                FROM graduation_requirement_categories
                WHERE requirement_id = ?
                ORDER BY id
                """;
        List<Integer> categoryIds = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requirementId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categoryIds.add(resultSet.getInt("category_id"));
                }
            }
        }

        return categoryIds;
    }

    private void setLegacyCategoryId(PreparedStatement statement, int parameterIndex, GraduationRequirement requirement) throws SQLException {
        List<Integer> categoryIds = uniqueNonNull(requirement.getCategoryIds());

        if (categoryIds.isEmpty()) {
            statement.setNull(parameterIndex, Types.INTEGER);
            return;
        }

        statement.setInt(parameterIndex, categoryIds.get(0));
    }

    private GraduationRequirement mapRow(Connection connection, ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        Integer curriculumId = resultSet.getInt("curriculum_id");
        String name = resultSet.getString("name");
        double requiredCredits = resultSet.getDouble("required_credits");
        List<Integer> categoryIds = findCategoryIds(connection, id);
        return new GraduationRequirement(id, curriculumId, categoryIds, name, requiredCredits);
    }
}
