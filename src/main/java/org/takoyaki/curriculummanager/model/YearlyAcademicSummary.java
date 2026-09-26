package org.takoyaki.curriculummanager.model;

/**
 * 年度ごとのGPAと修得単位を表示するためのモデル。
 */
public class YearlyAcademicSummary {

    private final int year;

    private final double gpa;

    private final double earnedCredits;

    public YearlyAcademicSummary(int year, double gpa, double earnedCredits) {

        this.year = year;

        this.gpa = gpa;

        this.earnedCredits = earnedCredits;
    }

    public int getYear() {

        return year;
    }

    public double getGpa() {

        return gpa;
    }

    public double getEarnedCredits() {

        return earnedCredits;
    }
}
