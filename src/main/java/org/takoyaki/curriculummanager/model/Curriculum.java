package org.takoyaki.curriculummanager.model;

/**
 * カリキュラムを表すモデルクラス。
 *
 * <p>
 * カリキュラムは1つの学科に所属します。
 * </p>
 */
public class Curriculum {

    /**
     * カリキュラムID。
     */
    private Integer id;

    /**
     * 所属する学科のID。
     */
    private Integer majorId;

    /**
     * カリキュラム名。
     */
    private String name;

    /**
     * カリキュラムの開始年度。
     */
    private int startYear;

    /**
     * 新規作成用コンストラクタ。
     *
     * @param majorId   所属する学科のID
     * @param name      カリキュラム名
     * @param startYear 開始年度
     */
    public Curriculum(Integer majorId, String name, int startYear) {
        this.majorId = majorId;
        this.name = name;
        this.startYear = startYear;
    }

    /**
     * データベースから取得したカリキュラム用コンストラクタ。
     *
     * @param id        カリキュラムID
     * @param majorId   所属する学科のID
     * @param name      カリキュラム名
     * @param startYear 開始年度
     */
    public Curriculum(Integer id, Integer majorId, String name, int startYear) {
        this.id = id;
        this.majorId = majorId;
        this.name = name;
        this.startYear = startYear;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMajorId() {
        return majorId;
    }

    public void setMajorId(Integer majorId) {
        this.majorId = majorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStartYear() {
        return startYear;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    /**
     * ComboBoxなどで表示する文字列。
     */
    @Override
    public String toString() {
        return name;
    }
}
