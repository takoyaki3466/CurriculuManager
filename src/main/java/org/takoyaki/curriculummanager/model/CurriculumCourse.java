package org.takoyaki.curriculummanager.model;

/**
 * カリキュラムと科目の関連を表すモデル。
 *
 * <p>
 * 1つのカリキュラムに対して、
 * どの科目を登録しているかを管理します。
 * </p>
 *
 * <p>
 * また、その科目が
 * 「必修」なのか「選択」なのかも管理します。
 * </p>
 *
 * <pre>
 *
 * カリキュラム
 * ```
 * ↓
 * ```
 * CurriculumCourse
 * ```
 * ├─ curriculumId
 * ```
 * ```
 * ├─ courseId
 * ```
 * ```
 * └─ requirementType
 * ```
 * </pre>
 */
public class CurriculumCourse {

    /**
     * カリキュラム科目関連ID。
     */
    private Integer id;

    /**
     * カリキュラムID。
     */
    private Integer curriculumId;

    /**
     * 科目ID。
     */
    private Integer courseId;

    /**
     * 必修・選択などの区分。
     *
     * <p>
     * 現在は文字列として保存します。
     * </p>
     *
     * <p>
     * 例：
     * </p>
     *
     * <ul>
     *     <li>必修</li>
     *     <li>選択</li>
     * </ul>
     */
    private String requirementType;

    /**
     * 新しいカリキュラム科目関連を作成します。
     *
     * @param curriculumId    カリキュラムID
     * @param courseId        科目ID
     * @param requirementType 必修・選択などの区分
     */
    public CurriculumCourse(Integer curriculumId, Integer courseId, String requirementType) {

        this.curriculumId = curriculumId;

        this.courseId = courseId;

        this.requirementType = requirementType;
    }

    /**
     * IDを含めてカリキュラム科目関連を作成します。
     *
     * @param id              カリキュラム科目関連ID
     * @param curriculumId    カリキュラムID
     * @param courseId        科目ID
     * @param requirementType 必修・選択などの区分
     */
    public CurriculumCourse(Integer id, Integer curriculumId, Integer courseId, String requirementType) {

        this.id = id;

        this.curriculumId = curriculumId;

        this.courseId = courseId;

        this.requirementType = requirementType;
    }

    /**
     * カリキュラム科目関連IDを取得します。
     *
     * @return カリキュラム科目関連ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * カリキュラム科目関連IDを設定します。
     *
     * @param id カリキュラム科目関連ID
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * カリキュラムIDを取得します。
     *
     * @return カリキュラムID
     */
    public Integer getCurriculumId() {
        return curriculumId;
    }

    /**
     * カリキュラムIDを設定します。
     *
     * @param curriculumId カリキュラムID
     */
    public void setCurriculumId(Integer curriculumId) {
        this.curriculumId = curriculumId;
    }

    /**
     * 科目IDを取得します。
     *
     * @return 科目ID
     */
    public Integer getCourseId() {
        return courseId;
    }

    /**
     * 科目IDを設定します。
     *
     * @param courseId 科目ID
     */
    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    /**
     * 必修・選択などの区分を取得します。
     *
     * @return 必修・選択などの区分
     */
    public String getRequirementType() {
        return requirementType;
    }

    /**
     * 必修・選択などの区分を設定します。
     *
     * @param requirementType 必修・選択などの区分
     */
    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    @Override
    public String toString() {
        return "CurriculumCourse{" + "id=" + id + ", curriculumId=" + curriculumId + ", courseId=" + courseId + ", requirementType='" + requirementType + '\'' + '}';
    }
}
