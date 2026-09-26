package org.takoyaki.curriculummanager.database.tables;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public final class AppSettingsTable {
    private AppSettingsTable() {
    }

    public static void createAppSettingsTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS app_settings (
                    setting_key TEXT PRIMARY KEY,
                    setting_value TEXT NOT NULL
                )
                """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
