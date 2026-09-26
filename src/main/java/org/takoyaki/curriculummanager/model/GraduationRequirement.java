package org.takoyaki.curriculummanager.model;

import java.util.ArrayList;
import java.util.List;

public class GraduationRequirement {
    private Integer id;
    private Integer curriculumId;
    private List<Integer> categoryIds;
    private String name;
    private double requiredCredits;

    public GraduationRequirement(Integer curriculumId, Integer categoryId, String name, double requiredCredits) {
        this.curriculumId = curriculumId;
        this.categoryIds = new ArrayList<>();

        if (categoryId != null) {
            this.categoryIds.add(categoryId);
        }

        this.name = name;
        this.requiredCredits = requiredCredits;
    }

    public GraduationRequirement(Integer curriculumId, List<Integer> categoryIds, String name, double requiredCredits) {
        this.curriculumId = curriculumId;
        this.categoryIds = categoryIds == null ? new ArrayList<>() : new ArrayList<>(categoryIds);
        this.name = name;
        this.requiredCredits = requiredCredits;
    }

    public GraduationRequirement(Integer id, Integer curriculumId, Integer categoryId, String name, double requiredCredits) {
        this.id = id;
        this.curriculumId = curriculumId;
        this.categoryIds = new ArrayList<>();

        if (categoryId != null) {
            this.categoryIds.add(categoryId);
        }

        this.name = name;
        this.requiredCredits = requiredCredits;
    }

    public GraduationRequirement(Integer id, Integer curriculumId, List<Integer> categoryIds, String name, double requiredCredits) {
        this.id = id;
        this.curriculumId = curriculumId;
        this.categoryIds = categoryIds == null ? new ArrayList<>() : new ArrayList<>(categoryIds);
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
        if (categoryIds == null || categoryIds.isEmpty()) {
            return null;
        }

        return categoryIds.get(0);
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryIds = new ArrayList<>();

        if (categoryId != null) {
            this.categoryIds.add(categoryId);
        }
    }

    public List<Integer> getCategoryIds() {
        return new ArrayList<>(categoryIds);
    }

    public void setCategoryIds(List<Integer> categoryIds) {
        this.categoryIds = categoryIds == null ? new ArrayList<>() : new ArrayList<>(categoryIds);
    }

    public boolean isAllCategories() {
        return categoryIds == null || categoryIds.isEmpty();
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
