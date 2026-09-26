package org.takoyaki.curriculummanager.repository.interfaces;

public interface SettingRepository {
    String findValue(String key);

    void saveValue(String key, String value);
}
