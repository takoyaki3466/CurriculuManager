package org.takoyaki.curriculummanager.model;

public class CurriculumCourse {
    private Integer id;
    private Integer curriculumId;
    private Integer courseId;
    private String requirementType;

    public CurriculumCourse(Integer curriculumId, Integer courseId, String requirementType) {
        this.curriculumId = curriculumId;
        this.courseId = courseId;
        this.requirementType = requirementType;
    }

    public CurriculumCourse(Integer id, Integer curriculumId, Integer courseId, String requirementType) {
        this.id = id;
        this.curriculumId = curriculumId;
        this.courseId = courseId;
        this.requirementType = requirementType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCurriculumId() {
        return curriculumId;
    }

    public void setCurriculumId(Integer curriculumId) {
        this.curriculumId = curriculumId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getRequirementType() {
        return requirementType;
    }

    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    @Override
    public String toString() {
        return "CurriculumCourse{" + "id=" + id + ", curriculumId=" + curriculumId + ", courseId=" + courseId + ", requirementType='" + requirementType + '\'' + '}';
    }
}
