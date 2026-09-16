package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.GraduationRequirement;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.model.MandatoryCourseStatus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ダッシュボードに表示する情報を管理するサービス。
 *
 * <p>
 * 学部・学科・カリキュラム・卒業要件・必修科目を
 * 一覧表示用の文字列へ変換する。
 * </p>
 */
public class DashboardService {

    private final CurriculumService curriculumService;

    private final GraduationRequirementService graduationRequirementService;

    public DashboardService() {

        curriculumService = new CurriculumService();

        graduationRequirementService = new GraduationRequirementService();
    }

    /**
     * ダッシュボード右側に表示する
     * カリキュラム・卒業要件一覧を取得する。
     *
     * <p>
     * 表示順は以下のようになる。
     * </p>
     *
     * <pre>
     * 学部名
     *   学科名
     *     カリキュラム名
     *       卒業要件
     *       卒業要件
     *       必修科目
     *       必修科目
     *     カリキュラム名
     *       ...
     * </pre>
     */
    public List<String> getCurriculumRequirementList() throws SQLException {

        List<String> lines = new ArrayList<>();

        /*
         * すべての学部を取得する。
         */
        List<Department> departments = curriculumService.getDepartments();

        /*
         * 学部ごとに処理する。
         */
        for (Department department : departments) {

            /*
             * 学部名を表示する。
             */
            lines.add(department.getName());

            /*
             * その学部に所属する学科を取得する。
             */
            List<Major> majors = curriculumService.getAllMajors().stream().filter(major -> department.getId().equals(major.getDepartmentId())).toList();

            /*
             * 学科ごとに処理する。
             */
            for (Major major : majors) {

                /*
                 * 学科名を表示する。
                 */
                lines.add("  " + major.getName());

                /*
                 * 学科に属するカリキュラムを取得する。
                 */
                List<Curriculum> curricula = curriculumService.getCurricula(major.getId());

                /*
                 * カリキュラムごとに処理する。
                 */
                for (Curriculum curriculum : curricula) {

                    /*
                     * カリキュラム名を表示する。
                     */
                    lines.add("    " + curriculum.getName());

                    /*
                     * 卒業要件を取得する。
                     */
                    List<GraduationRequirement> requirements = graduationRequirementService.getRequirements(curriculum.getId());

                    /*
                     * 卒業要件を1件ずつ表示する。
                     *
                     * 例:
                     *       基礎科目 20.0単位
                     *       専門科目 60.0単位
                     */
                    for (GraduationRequirement requirement : requirements) {

                        lines.add("      " + requirement.getName() + " " + formatCredits(requirement.getRequiredCredits()) + "単位");
                    }

                    /*
                     * 必修科目を取得する。
                     */
                    List<MandatoryCourseStatus> mandatoryCourses = graduationRequirementService.getMandatoryCourseStatuses(curriculum.getId());

                    /*
                     * 必修科目を1件ずつ表示する。
                     *
                     * 例:
                     *       数学Ⅰ 2.0単位 必修
                     */
                    for (MandatoryCourseStatus course : mandatoryCourses) {

                        lines.add("      " + course.getCourseName() + " " + formatCredits(course.getCredits()) + "単位 必修");
                    }
                }
            }
        }

        return lines;
    }

    /**
     * 単位数を表示用の文字列に変換する。
     *
     * <p>
     * 2.0 のような値は 2、
     * 2.5 のような値は 2.5 と表示する。
     * </p>
     */
    private String formatCredits(double credits) {

        if (credits == Math.floor(credits)) {

            return String.valueOf((int) credits);
        }

        return String.valueOf(credits);
    }
}
