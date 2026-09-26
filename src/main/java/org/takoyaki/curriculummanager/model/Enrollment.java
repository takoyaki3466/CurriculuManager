package org.takoyaki.curriculummanager.model;

public class Enrollment {
    private Integer id;
    private Integer curriculumId;
    private Integer courseId;
    private int year;
    private String semester;
    private Integer gradeId;

    public Enrollment(Integer curriculumId, Integer courseId, int year, String semester, Integer gradeId) {
        this.curriculumId = curriculumId;
        this.courseId = courseId;
        this.year = year;
        this.semester = semester;
        this.gradeId = gradeId;
    }

    public Enrollment(Integer id, Integer curriculumId, Integer courseId, int year, String semester, Integer gradeId) {
        this.id = id;
        this.curriculumId = curriculumId;
        this.courseId = courseId;
        this.year = year;
        this.semester = semester;
        this.gradeId = gradeId;
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

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }
}
