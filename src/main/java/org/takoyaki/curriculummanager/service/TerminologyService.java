package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Terminology;
import org.takoyaki.curriculummanager.repository.AppSettingRepository;
import org.takoyaki.curriculummanager.repository.interfaces.SettingRepository;

public class TerminologyService {
    private static final String CURRICULUM_NAME_KEY = "curriculum_display_name";
    private final SettingRepository settingRepository;

    public TerminologyService() {
        this(new AppSettingRepository());
    }

    public TerminologyService(SettingRepository settingRepository) {
        this.settingRepository = settingRepository;
    }

    public Terminology getTerminology() {
        return new Terminology(settingRepository.findValue(CURRICULUM_NAME_KEY));
    }

    public boolean isConfigured() {
        String curriculumName = settingRepository.findValue(CURRICULUM_NAME_KEY);
        return curriculumName != null && !curriculumName.isBlank();
    }

    public void saveTerminology(String curriculumName) {
        Terminology terminology = new Terminology(curriculumName);
        settingRepository.saveValue(CURRICULUM_NAME_KEY, terminology.curriculumName());
    }
}
