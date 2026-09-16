package org.takoyaki.curriculummanager.model;

/**
 * 履修情報を表すモデルクラス。
 * <p>
 * 1つの履修情報は、
 * <p>
 * ・どのカリキュラムか
 * ・どの科目か
 * ・何年度か
 * ・何学期か
 * ・どの成績か
 * <p>
 * を保持する。
 */
public class Enrollment {

    /**
     * 履修情報のID。
     * <p>
     * データベース登録前はnull。
     */
    private Integer id;

    /**
     * この履修が所属するカリキュラムID。
     */
    private Integer curriculumId;

    /**
     * 科目ID。
     */
    private Integer courseId;

    /**
     * 履修年度。
     */
    private int year;

    /**
     * 学期。
     * <p>
     * 例：
     * 「前期」
     * 「後期」
     */
    private String semester;

    /**
     * 成績ID。
     * <p>
     * 成績未入力の場合はnull。
     */
    private Integer gradeId;

    /**
     * 新規履修情報を作成する。
     */
    public Enrollment(Integer curriculumId, Integer courseId, int year, String semester, Integer gradeId) {
        this.curriculumId = curriculumId;
        this.courseId = courseId;
        this.year = year;
        this.semester = semester;
        this.gradeId = gradeId;
    }

    /**
     * データベースから読み込んだ履修情報を作成する。
     */
    public Enrollment(Integer id, Integer curriculumId, Integer courseId, int year, String semester, Integer gradeId) {
        this.id = id;
        this.curriculumId = curriculumId;
        this.courseId = courseId;
        this.year = year;
        this.semester = semester;
        this.gradeId = gradeId;
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

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public Integer getGradeId() {
        return gradeId;
    }

    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }
}
