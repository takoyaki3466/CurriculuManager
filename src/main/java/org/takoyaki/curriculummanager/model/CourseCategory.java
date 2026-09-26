package org.takoyaki.curriculummanager.model;

public class CourseCategory {
    private Integer id;
    private Integer curriculumId;
    private Integer parentId;
    private String name;
    private int displayOrder;

    public CourseCategory(Integer curriculumId, Integer parentId, String name, int displayOrder) {
        this.curriculumId = curriculumId;
        this.parentId = parentId;
        this.name = name;
        this.displayOrder = displayOrder;
    }

    public CourseCategory(Integer id, Integer curriculumId, Integer parentId, String name, int displayOrder) {
        this.id = id;
        this.curriculumId = curriculumId;
        this.parentId = parentId;
        this.name = name;
        this.displayOrder = displayOrder;
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public String toString() {
        return name;
    }
}
