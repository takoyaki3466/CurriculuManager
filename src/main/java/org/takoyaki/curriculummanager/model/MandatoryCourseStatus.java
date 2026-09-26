package org.takoyaki.curriculummanager.model;

public class MandatoryCourseStatus {
    private final Integer courseId;
    private final String courseCode;
    private final String courseName;
    private final double credits;
    private final boolean passed;

    public MandatoryCourseStatus(Integer courseId, String courseCode, String courseName, double credits, boolean passed) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.passed = passed;
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

    public boolean isPassed() {
        return passed;
    }

    @Override
    public String toString() {
        return courseName;
    }
}
