package org.takoyaki.curriculummanager.model;

/**
 * 必修科目の修得状況を表す表示用モデル。
 *
 * <p>
 * データベースへ直接保存するモデルではなく、
 * 卒業要件画面などで必修科目の状態を表示するために使用する。
 */
public class MandatoryCourseStatus {

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
     * 修得済みかどうか。
     */
    private final boolean passed;

    /**
     * 必修科目の状態を生成する。
     *
     * @param courseId   科目ID
     * @param courseCode 科目コード
     * @param courseName 科目名
     * @param credits    単位数
     * @param passed     修得済みかどうか
     */
    public MandatoryCourseStatus(Integer courseId, String courseCode, String courseName, double credits, boolean passed) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
        this.passed = passed;
    }

    /**
     * 科目IDを取得する。
     *
     * @return 科目ID
     */
    public Integer getCourseId() {
        return courseId;
    }

    /**
     * 科目コードを取得する。
     *
     * @return 科目コード
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * 科目名を取得する。
     *
     * @return 科目名
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * 単位数を取得する。
     *
     * @return 単位数
     */
    public double getCredits() {
        return credits;
    }

    /**
     * 修得済みかどうかを取得する。
     *
     * @return 修得済みならtrue
     */
    public boolean isPassed() {
        return passed;
    }

    /**
     * 画面表示用の文字列を返す。
     */
    @Override
    public String toString() {
        return courseName;
    }
}
