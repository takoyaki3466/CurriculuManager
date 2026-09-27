package org.takoyaki.curriculummanager.model;

import org.takoyaki.curriculummanager.i18n.I18n;

public class Curriculum {
    private static final int ALL_OPTION_ID = -1;
    private Integer id;
    private Integer majorId;
    private String name;
    private int startYear;

    public Curriculum(Integer majorId, String name, int startYear) {
        this.majorId = majorId;
        this.name = name;
        this.startYear = startYear;
    }

    public Curriculum(Integer id, Integer majorId, String name, int startYear) {
        this.id = id;
        this.majorId = majorId;
        this.name = name;
        this.startYear = startYear;
    }

    public static Curriculum allOption() {
        return new Curriculum(ALL_OPTION_ID, null, I18n.raw("common.selectAll"), 0);
    }

    public boolean isAllOption() {
        return Integer.valueOf(ALL_OPTION_ID).equals(id);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMajorId() {
        return majorId;
    }

    public void setMajorId(Integer majorId) {
        this.majorId = majorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    @Override
    public String toString() {
        return name;
    }
}
