package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.repository.abstracts.AbstractJdbcRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DepartmentRepository extends AbstractJdbcRepository<Department> {
    public Department save(Department department) throws SQLException {
        String sql = """
                INSERT INTO departments (name)
                VALUES (?)
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, department.getName());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    department.setId(resultSet.getInt(1));
                }
            }
        }

        return department;
    }

    public Department findById(Integer id) throws SQLException {
        String sql = """
                SELECT
                    id,
                    name
                FROM departments
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapRow(resultSet);
                }
            }
        }

        return null;
    }

    public List<Department> findAll() throws SQLException {
        String sql = """
                SELECT
                    id,
                    name
                FROM departments
                ORDER BY id
                """;
        List<Department> departments = new ArrayList<>();

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                departments.add(mapRow(resultSet));
            }
        }

        return departments;
    }

    public void update(Department department) throws SQLException {
        String sql = """
                UPDATE departments
                SET name = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, department.getName());
            statement.setInt(2, department.getId());
            statement.executeUpdate();
        }
    }

    public void deleteById(Integer id) throws SQLException {
        String sql = """
                DELETE FROM departments
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private Department mapRow(ResultSet resultSet) throws SQLException {
        Integer id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        return new Department(id, name);
    }
}
