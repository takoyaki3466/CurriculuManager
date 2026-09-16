package org.takoyaki.curriculummanager.model;

/**
 * 学科を表すモデルクラス。
 * <p>
 * 例:
 * 機械工学科
 * 電気電子工学科
 * <p>
 * 学科そのものを表すクラスであり、
 * 年度ごとの卒業要件などは Curriculum が担当します。
 */
public class Department {
    private Integer id;
    private String name;

    public Department(String name) {
        this.name = name;
    }
    public Department(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}