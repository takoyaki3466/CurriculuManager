package org.takoyaki.curriculummanager.model;

/**
 * カリキュラム科目設定画面で使用する
 * 表示専用モデルです。
 *
 * <p>
 * CurriculumCourseは、
 * カリキュラムID・科目ID・必修/選択などの
 * 関連情報だけを保持しています。
 * </p>
 *
 * <p>
 * このクラスでは、画面に表示するために
 * 科目コード・科目名・単位数などもまとめて保持します。
 * </p>
 *
 * <pre>
 *
 * CurriculumCourse
 * ```
 * +
 * ```
 * Course
 * ```
 * ↓
 * ```
 * CurriculumCourseDisplay
 * </pre>
 */
public class CurriculumCourseDisplay {

    /**
     * カリキュラム科目関連ID。
     */
    private final Integer id;

    /**
     * カリキュラムID。
     */
    private final Integer curriculumId;

    /**
     * 科目ID。
     */
    private final Integer courseId;

    /**
     * 科目コード。
     */
    private final String courseCode;

    /**
     * 科目名。
     */
    private final String courseName;

    /**
     * 単位数。
     */
    private final double credits;

    /**
     * カテゴリID。
     */
    private final Integer categoryId;

    /**
     * 必修・選択などの区分。
     */
    private String requirementType;

    /**
     * カリキュラム科目表示モデルを作成します。
     *
     * @param id              カリキュラム科目関連ID
     * @param curriculumId    カリキュラムID
     * @param courseId        科目ID
     * @param courseCode      科目コード
     * @param courseName      科目名
     * @param credits         単位数
     * @param categoryId      カテゴリID
     * @param requirementType 必修・選択などの区分
     */
    public CurriculumCourseDisplay(Integer id, Integer curriculumId, Integer courseId, String courseCode, String courseName, double credits, Integer categoryId, String requirementType) {

        this.id = id;

        this.curriculumId = curriculumId;

        this.courseId = courseId;

        this.courseCode = courseCode;

        this.courseName = courseName;

        this.credits = credits;

        this.categoryId = categoryId;

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
     * カリキュラムIDを取得します。
     *
     * @return カリキュラムID
     */
    public Integer getCurriculumId() {
        return curriculumId;
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
     * 科目コードを取得します。
     *
     * @return 科目コード
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * 科目名を取得します。
     *
     * @return 科目名
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * 単位数を取得します。
     *
     * @return 単位数
     */
    public double getCredits() {
        return credits;
    }

    /**
     * カテゴリIDを取得します。
     *
     * @return カテゴリID
     */
    public Integer getCategoryId() {
        return categoryId;
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
     * 必修・選択などの区分を変更します。
     *
     * <p>
     * GUI上で必修・選択を変更した場合に使用します。
     * </p>
     *
     * @param requirementType 新しい区分
     */
    public void setRequirementType(String requirementType) {
        this.requirementType = requirementType;
    }

    /**
     * 科目名を表示用文字列として返します。
     *
     * @return 科目名
     */
    @Override
    public String toString() {
        return courseName;
    }
}
