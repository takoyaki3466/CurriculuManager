package org.takoyaki.curriculummanager.model;

/**
 * 学科を表すモデルクラス。
 *
 * <p>
 * 学科は1つの学部に所属します。
 * </p>
 */
public class Major {

    /**
     * 学科ID。
     */
    private Integer id;

    /**
     * 所属する学部のID。
     */
    private Integer departmentId;

    /**
     * 学科名。
     */
    private String name;

    /**
     * 新規作成用コンストラクタ。
     *
     * @param departmentId 所属する学部のID
     * @param name         学科名
     */
    public Major(Integer departmentId, String name) {
        this.departmentId = departmentId;
        this.name = name;
    }

    /**
     * データベースから取得した学科用コンストラクタ。
     *
     * @param id           学科ID
     * @param departmentId 所属する学部のID
     * @param name         学科名
     */
    public Major(Integer id, Integer departmentId, String name) {
        this.id = id;
        this.departmentId = departmentId;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * ComboBoxなどで表示する文字列。
     */
    @Override
    public String toString() {
        return name;
    }
}
