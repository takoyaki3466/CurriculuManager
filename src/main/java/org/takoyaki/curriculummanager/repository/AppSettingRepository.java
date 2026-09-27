package org.takoyaki.curriculummanager.repository;

import org.takoyaki.curriculummanager.database.DatabaseManager;
import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.repository.interfaces.SettingRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AppSettingRepository implements SettingRepository {
    @Override
    public String findValue(String key) {
        String sql = "SELECT setting_value FROM app_settings WHERE setting_key = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("settings.error.load"), e);
        }
    }

    @Override
    public void saveValue(String key, String value) {
        String sql = """
                INSERT INTO app_settings (setting_key, setting_value)
                VALUES (?, ?)
                ON CONFLICT(setting_key) DO UPDATE SET setting_value = excluded.setting_value
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key);
            statement.setString(2, value);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(I18n.raw("settings.error.save"), e);
        }
    }
}
