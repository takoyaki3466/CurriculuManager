package org.takoyaki.curriculummanager.model;

/**
 * 卒業要件を表すモデルクラスです。
 * <p>
 * カリキュラム全体、または特定のカテゴリーについて
 * 必要な単位数を設定できます。
 * <p>
 * 例:
 * <p>
 * 一般教養 → 12単位
 * グループ1 → 4単位
 * 基礎教育 → 10単位
 * 専門科目 → 40単位
 */
public class GraduationRequirement {
    private Integer id;
    private Integer curriculumId;
    private Integer categoryId;
    private String name;
    private double requiredCredits;

    public GraduationRequirement(Integer curriculumId, Integer categoryId, String name, double requiredCredits) {
        this.curriculumId = curriculumId;
        this.categoryId = categoryId;
        this.name = name;
        this.requiredCredits = requiredCredits;
    }

    public GraduationRequirement(Integer id, Integer curriculumId, Integer categoryId, String name, double requiredCredits) {
        this.id = id;
        this.curriculumId = curriculumId;
        this.categoryId = categoryId;
        this.name = name;
        this.requiredCredits = requiredCredits;
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

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRequiredCredits() {
        return requiredCredits;
    }

    public void setRequiredCredits(double requiredCredits) {
        this.requiredCredits = requiredCredits;
    }

    @Override
    public String toString() {
        return name;
    }
}