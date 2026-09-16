package org.takoyaki.curriculummanager.model;

/**
 * 卒業要件画面に表示する情報。
 */
public class GraduationRequirementDisplay {
    private final String name;
    private final double requiredCredits;
    private final double earnedCredits;

    public GraduationRequirementDisplay(String name, double requiredCredits, double earnedCredits) {
        this.name = name;
        this.requiredCredits = requiredCredits;
        this.earnedCredits = earnedCredits;
    }

    public String getName() {
        return name;
    }

    public double getRequiredCredits() {
        return requiredCredits;
    }

    public double getEarnedCredits() {
        return earnedCredits;
    }

    public double getRemainingCredits() {

        return Math.max(0.0, requiredCredits - earnedCredits);
    }

    public boolean isSatisfied() {

        return earnedCredits >= requiredCredits;
    }

    public String getStatus() {

        return isSatisfied() ? "達成" : "未達成";
    }
}
