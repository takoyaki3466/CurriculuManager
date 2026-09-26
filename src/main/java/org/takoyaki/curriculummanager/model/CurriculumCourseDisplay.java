package org.takoyaki.curriculummanager.model;

public class CurriculumCourseDisplay {
    private final Integer id;
    private final Integer curriculumId;
    private final Integer courseId;
    private final String courseCode;
    private final String courseName;
    private final double credits;
    private final Integer categoryId;
    private String requirementType;

    public CurriculumCourseDisplay(Integer id, Integer curriculumId, Integer courseId, String courseCode, String courseName, double credits, Integer categoryId, String requirementType) {
        this.id = id;
        this.curriculumId = curriculumId;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.categoryId = categoryId;
        this.requirementType = requirementType;
    }

    public Integer getId() {
        return id;
    }

    public Integer getCurriculumId() {
        return curriculumId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public double getCredits() {
        return credits;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getRequirementType() {
        return requirementType;
    }

    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    @Override
    public String toString() {
        return courseName;
    }
}
