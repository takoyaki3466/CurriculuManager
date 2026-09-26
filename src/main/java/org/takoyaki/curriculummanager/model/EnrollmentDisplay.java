package org.takoyaki.curriculummanager.model;

import java.util.Objects;

public class EnrollmentDisplay {
    private final Integer enrollmentId;
    private final int year;
    private final String semester;
    private final Integer courseId;
    private final String courseCode;
    private final String courseName;
    private final double credits;
    private final Integer gradeId;
    private final String gradeSymbol;

    public EnrollmentDisplay(Integer enrollmentId, int year, String semester, Integer courseId, String courseCode, String courseName, double credits, Integer gradeId, String gradeSymbol) {
        this.enrollmentId = enrollmentId;
        this.year = year;
        this.semester = semester;
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.gradeId = gradeId;
        this.gradeSymbol = gradeSymbol;
    }

    public Integer getEnrollmentId() {
        return enrollmentId;
    }

    public int getYear() {
        return year;
    }

    public String getSemester() {
        return semester;
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

    public Integer getGradeId() {
        return gradeId;
    }

    public String getGradeSymbol() {
        return gradeSymbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (EnrollmentDisplay) obj;
        return Objects.equals(this.enrollmentId, that.enrollmentId) &&
                this.year == that.year &&
                Objects.equals(this.semester, that.semester) &&
                Objects.equals(this.courseId, that.courseId) &&
                Objects.equals(this.courseCode, that.courseCode) &&
                Objects.equals(this.courseName, that.courseName) &&
                Double.doubleToLongBits(this.credits) == Double.doubleToLongBits(that.credits) &&
                Objects.equals(this.gradeId, that.gradeId) &&
                Objects.equals(this.gradeSymbol, that.gradeSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId, year, semester, courseId, courseCode, courseName, credits, gradeId, gradeSymbol);
    }

    @Override
    public String toString() {
        return "EnrollmentDisplay[" +
                "enrollmentId=" + enrollmentId + ", " +
                "year=" + year + ", " +
                "semester=" + semester + ", " +
                "courseId=" + courseId + ", " +
                "courseCode=" + courseCode + ", " +
                "courseName=" + courseName + ", " +
                "credits=" + credits + ", " +
                "gradeId=" + gradeId + ", " +
                "gradeSymbol=" + gradeSymbol + ']';
    }
}
