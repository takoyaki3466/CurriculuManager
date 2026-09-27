package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.util.JsonUtils;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DatabaseTransferService {
    private static final String FORMAT_NAME = "CurriculumManager";
    private static final long MAX_IMPORT_SIZE = 50L * 1024L * 1024L;
    private static final List<String> TABLES = List.of(
            "departments",
            "majors",
            "app_settings",
            "curricula",
            "course_categories",
            "courses",
            "curriculum_courses",
            "graduation_requirements",
            "graduation_requirement_categories",
            "grade_definitions",
            "enrollments");

    public void exportDatabase(Path destination) throws IOException, SQLException {
        if (destination == null) {
            throw new IllegalArgumentException(I18n.raw("databaseTransfer.error.destinationRequired"));
        }

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("format", FORMAT_NAME);
        root.put("version", 1);
        Map<String, Object> tables = new LinkedHashMap<>();

        try (Connection connection = DatabaseManager.getConnection()) {
            for (String table : TABLES) {
                tables.put(table, readTable(connection, table));
            }
        }

        root.put("tables", tables);
        Files.writeString(
                destination,
                JsonUtils.write(root),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING,
                StandardOpenOption.WRITE);
    }

    public void importDatabase(Path source) throws IOException, SQLException {
        if (source == null) {
            throw new IllegalArgumentException(I18n.raw("databaseTransfer.error.sourceRequired"));
        }

        if (!Files.isRegularFile(source)) {
            throw new IllegalArgumentException(I18n.raw("databaseTransfer.error.fileMissing"));
        }

        if (Files.size(source) > MAX_IMPORT_SIZE) {
            throw new IllegalArgumentException(I18n.raw("databaseTransfer.error.fileTooLarge"));
        }

        String json = Files.readString(source, StandardCharsets.UTF_8);

        if (!json.isEmpty() && json.charAt(0) == '\uFEFF') {
            json = json.substring(1);
        }

        Map<String, Object> tables = validateRoot(JsonUtils.parse(json));

        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = OFF");
            connection.setAutoCommit(false);

            try {
                List<String> reverseTables = new ArrayList<>(TABLES);
                Collections.reverse(reverseTables);

                for (String table : reverseTables) {
                    statement.executeUpdate("DELETE FROM " + table);
                }

                statement.executeUpdate("DELETE FROM sqlite_sequence");

                for (String table : TABLES) {
                    insertRows(connection, table, requireRows(tables.get(table), table));
                }

                try (ResultSet violations = statement.executeQuery("PRAGMA foreign_key_check")) {
                    if (violations.next()) {
                        throw new SQLException(I18n.raw("databaseTransfer.error.referenceConflict"));
                    }
                }

                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
                statement.execute("PRAGMA foreign_keys = ON");
            }
        }
    }

    private List<Map<String, Object>> readTable(Connection connection, String table) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " ORDER BY rowid");
             ResultSet resultSet = statement.executeQuery()) {
            ResultSetMetaData metadata = resultSet.getMetaData();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();

                for (int index = 1; index <= metadata.getColumnCount(); index++) {
                    row.put(metadata.getColumnName(index), resultSet.getObject(index));
                }

                rows.add(row);
            }
        }

        return rows;
    }

    private Map<String, Object> validateRoot(Object value) {
        Map<String, Object> root = requireObject(value, I18n.raw("databaseTransfer.error.rootObject"));

        if (!FORMAT_NAME.equals(root.get("format"))) {
            throw new IllegalArgumentException(I18n.raw("databaseTransfer.error.notExport"));
        }

        Object version = root.get("version");

        if (!(version instanceof Number number) || number.intValue() != 1) {
            throw new IllegalArgumentException(I18n.raw("databaseTransfer.error.unsupported"));
        }

        Map<String, Object> tables = requireObject(root.get("tables"), I18n.raw("databaseTransfer.error.tablesMissing"));

        for (String table : TABLES) {
            requireRows(tables.get(table), table);
        }

        return tables;
    }

    private List<Map<String, Object>> requireRows(Object value, String table) {
        if (!(value instanceof List<?> values)) {
            throw new IllegalArgumentException(I18n.raw("databaseTransfer.error.tableFormat", table));
        }

        List<Map<String, Object>> rows = new ArrayList<>();

        for (Object item : values) {
            rows.add(requireObject(item, I18n.raw("databaseTransfer.error.rowFormat", table)));
        }

        return rows;
    }

    private Map<String, Object> requireObject(Object value, String message) {
        if (!(value instanceof Map<?, ?> values)) {
            throw new IllegalArgumentException(message);
        }

        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<?, ?> entry : values.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException(message);
            }

            result.put(key, entry.getValue());
        }

        return result;
    }

    private void insertRows(Connection connection, String table, List<Map<String, Object>> rows) throws SQLException {
        Set<String> validColumns = getColumns(connection, table);

        for (Map<String, Object> row : rows) {
            if (row.isEmpty() || !validColumns.containsAll(row.keySet())) {
                throw new IllegalArgumentException(I18n.raw("databaseTransfer.error.column", table));
            }

            List<String> columns = new ArrayList<>(row.keySet());
            String placeholders = String.join(", ", Collections.nCopies(columns.size(), "?"));
            String sql = "INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES (" + placeholders + ")";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                for (int index = 0; index < columns.size(); index++) {
                    Object item = row.get(columns.get(index));

                    if (item instanceof Boolean booleanValue) {
                        statement.setInt(index + 1, booleanValue ? 1 : 0);
                    } else {
                        statement.setObject(index + 1, item);
                    }
                }

                statement.executeUpdate();
            }
        }
    }

    private Set<String> getColumns(Connection connection, String table) throws SQLException {
        Set<String> columns = new LinkedHashSet<>();

        try (PreparedStatement statement = connection.prepareStatement("PRAGMA table_info(" + table + ")");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }

        return columns;
    }
}
