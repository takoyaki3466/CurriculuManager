package org.takoyaki.curriculummanager.model;

import org.takoyaki.curriculummanager.i18n.I18n;

public class GraduationRequirementDisplay {
    private final Integer requirementId;
    private final String name;
    private final String targetCategories;
    private final double requiredCredits;
    private final double earnedCredits;

    public GraduationRequirementDisplay(String name, String targetCategories, double requiredCredits, double earnedCredits) {
        this(null, name, targetCategories, requiredCredits, earnedCredits);
    }

    public GraduationRequirementDisplay(Integer requirementId, String name, String targetCategories, double requiredCredits, double earnedCredits) {
        this.requirementId = requirementId;
        this.name = name;
        this.targetCategories = targetCategories == null ? "" : targetCategories;
        this.requiredCredits = requiredCredits;
        this.earnedCredits = earnedCredits;
    }

    public GraduationRequirementDisplay(String name, double requiredCredits, double earnedCredits) {
        this(name, "", requiredCredits, earnedCredits);
    }

    public String getName() {
        return name;
    }

    public Integer getRequirementId() {
        return requirementId;
    }

    public String getTargetCategories() {
        return targetCategories;
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
        return isSatisfied()
                ? I18n.text("graduation.status.satisfied")
                : I18n.text("graduation.status.unsatisfied");
    }
}
