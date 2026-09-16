package org.takoyaki.curriculummanager.model;

/**
 * 科目カテゴリーを表すモデルクラス。
 * <p>
 * 科目カテゴリーは階層構造を持つことができます。
 * <p>
 * 例:
 * <p>
 * 一般教養
 * ├─ グループ1
 * ├─ グループ2
 * ├─ グループ3
 * └─ グループ4
 * <p>
 * 基礎教育
 * ├─ グローバルスキル
 * ├─ 数学
 * ├─ 物理
 * └─ 化学
 * <p>
 * 専門科目
 * ├─ 専門基礎
 * └─ 専門応用
 * <p>
 * 親カテゴリーを持たないカテゴリーは
 * parentId が null になります。
 */
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