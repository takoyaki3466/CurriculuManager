package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.GradeDef;
import org.takoyaki.curriculummanager.repository.abstracts.AbstractJdbcRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class GradeDefRepository extends AbstractJdbcRepository<GradeDef> {
    public GradeDef save(GradeDef gradeDef) throws SQLException {
        String sql = """
                INSERT INTO grade_definitions (
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                )
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, gradeDef.getSymbol());

            if (gradeDef.getGpaPoints() == null) {
                statement.setNull(2, java.sql.Types.REAL);
            } else {
                statement.setDouble(2, gradeDef.getGpaPoints());
            }

            statement.setInt(3, gradeDef.isPassed() ? 1 : 0);
            statement.setInt(4, gradeDef.isIncludedInGpa() ? 1 : 0);
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    gradeDef.setId(resultSet.getInt(1));
                }
            }
        }

        return gradeDef;
    }

    public GradeDef findById(Integer id) throws SQLException {
        String sql = """
                SELECT
                    id,
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                FROM grade_definitions
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

    public List<GradeDef> findAll() throws SQLException {
        String sql = """
                SELECT
                    id,
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                FROM grade_definitions
                ORDER BY id
                """;
        List<GradeDef> grades = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                grades.add(mapRow(resultSet));
            }
        }

        return grades;
    }

    public GradeDef findBySymbol(String symbol) throws SQLException {
        String sql = """
                SELECT
                    id,
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                FROM grade_definitions
                WHERE symbol = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, symbol);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    public List<GradeDef> findIncludedInGpa() throws SQLException {
        String sql = """
                SELECT
                    id,
                    symbol,
                    gpa_points,
                    passed,
                    included_in_gpa
                FROM grade_definitions
                WHERE included_in_gpa = 1
                ORDER BY id
                """;
        List<GradeDef> grades = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                grades.add(mapRow(resultSet));
            }
        }

        return grades;
    }

    public void update(GradeDef gradeDef) throws SQLException {
        String sql = """
                UPDATE grade_definitions
                SET
                    symbol = ?,
                    gpa_points = ?,
                    passed = ?,
                    included_in_gpa = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, gradeDef.getSymbol());

            if (gradeDef.getGpaPoints() == null) {
                statement.setNull(2, java.sql.Types.REAL);
            } else {
                statement.setDouble(2, gradeDef.getGpaPoints());
            }

            statement.setInt(3, gradeDef.isPassed() ? 1 : 0);
            statement.setInt(4, gradeDef.isIncludedInGpa() ? 1 : 0);
            statement.setInt(5, gradeDef.getId());
            statement.executeUpdate();
        }
    }

    public void deleteById(Integer id) throws SQLException {
        String sql = """
                DELETE FROM grade_definitions
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private GradeDef mapRow(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        String symbol = resultSet.getString("symbol");
        double gpaPointsValue = resultSet.getDouble("gpa_points");
        Double gpaPoints;

        if (resultSet.wasNull()) {
            gpaPoints = null;
        } else {
            gpaPoints = gpaPointsValue;
        }

        boolean passed = resultSet.getInt("passed") != 0;
        boolean includedInGpa = resultSet.getInt("included_in_gpa") != 0;
        return new GradeDef(id, symbol, gpaPoints, passed, includedInGpa);
    }
}
