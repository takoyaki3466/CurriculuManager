package org.takoyaki.curriculummanager.database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private static final String DATABASE_FILE_NAME = "curriculum.db";

    private static final Path DATA_DIRECTORY = Paths.get(System.getenv("LOCALAPPDATA"), "CurriculumManager");

    private static final Path DATABASE_PATH = DATA_DIRECTORY.resolve(DATABASE_FILE_NAME);

    private static final String JDBC_URL = "jdbc:sqlite:" + DATABASE_PATH;

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Files.createDirectories(DATA_DIRECTORY);
        } catch (Exception e) {
            throw new SQLException("データベースディレクトリを作成できませんでした。", e);
        }

        Connection connection = DriverManager.getConnection(JDBC_URL);

        /*
         * SQLiteの外部キー制約を有効化します。
         *
         * SQLiteでは外部キー制約がConnection単位で
         * 設定されるため、Connectionを作成するたびに
         * 有効化します。
         */
        try (Statement statement = connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    public static Path getDatabasePath() {
        return DATABASE_PATH;
    }
}