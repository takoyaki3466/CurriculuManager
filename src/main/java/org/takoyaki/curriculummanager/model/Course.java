package org.takoyaki.curriculummanager.model;

/**
 * 科目そのものを表すモデルクラス。
 * <p>
 * 科目は特定の学科だけに所属するものではなく、
 * システム全体で共有できるように設計します。
 * <p>
 * 例:
 * <p>
 * 科目名: プログラミング基礎
 * 科目コード: CS101
 * 単位数: 2
 * <p>
 * 「必修」「選択」などの情報はこのクラスには持たせません。
 * それらは CurriculumCourse が担当します。
 */
public class Course {
    private Integer id;
    private String courseCode;
    private String name;
    private double credits;
    private Integer categoryId;
    private String description;

    public Course(String courseCode, String name, double credits, Integer categoryId, String description) {
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.categoryId = categoryId;
        this.description = description;
    }

    public Course(Integer id, String courseCode, String name, double credits, Integer categoryId, String description) {
        this.id = id;
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.categoryId = categoryId;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCredits() {
        return credits;
    }

    public void setCredits(double credits) {
        this.credits = credits;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name;
    }
}