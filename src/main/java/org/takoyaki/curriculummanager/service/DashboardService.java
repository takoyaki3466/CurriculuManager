package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.GraduationRequirement;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.model.MandatoryCourseStatus;
import org.takoyaki.curriculummanager.repository.EnrollmentRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

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

    private final EnrollmentRepository enrollmentRepository;

    public DashboardService() {

        curriculumService = new CurriculumService();

        graduationRequirementService = new GraduationRequirementService();

        enrollmentRepository = new EnrollmentRepository();
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
                    /*
                     * 卒業要件を取得する。
                     */
                    List<GraduationRequirement> requirements = graduationRequirementService.getRequirements(curriculum.getId());

                    List<MandatoryCourseStatus> mandatoryCourses = graduationRequirementService.getMandatoryCourseStatuses(curriculum.getId());

                    String requirementStatus = requirements.isEmpty()

                            ? "未設定"

                            : graduationRequirementService.areRequirementsSatisfied(curriculum.getId()) ? "達成" : "未達成";

                    String mandatoryStatus = mandatoryCourses.isEmpty()

                            ? "対象なし"

                            : graduationRequirementService.isMandatoryCoursesSatisfied(curriculum.getId()) ? "達成" : "未達成";

                    String curriculumStatus = graduationRequirementService.isGraduated(curriculum.getId()) ? "卒業可能" : "卒業不可";

                    lines.add("    " + curriculum.getName() + "  [" + curriculumStatus

                            + " / 単位要件: " + requirementStatus

                            + " / 必修科目: " + mandatoryStatus + "]");

                    /*
                     * 卒業要件を1件ずつ表示する。
                     *
                     * 例:
                     *       基礎科目 20.0単位
                     *       専門科目 60.0単位
                     */
                    for (GraduationRequirement requirement : requirements) {

                        boolean satisfied = graduationRequirementService.getRemainingCredits(requirement) <= 0;

                        lines.add("      " + requirement.getName() + " " + formatCredits(requirement.getRequiredCredits()) + "単位  [" + (satisfied ? "達成" : "未達成") + "]");
                    }

                    /*
                     * 必修科目を1件ずつ表示する。
                     *
                     * 例:
                     *       数学Ⅰ 2.0単位 必修
                     */
                    for (MandatoryCourseStatus course : mandatoryCourses) {

                        lines.add("      " + course.getCourseName() + " " + formatCredits(course.getCredits()) + "単位 必修  [" + (course.isPassed() ? "達成" : "未達成") + "]");
                    }
                }
            }
        }

        return lines;
    }

    /**
     * 履修が登録されている年度を新しい順に取得する。
     */
    public List<Integer> getEnrollmentYears() throws SQLException {

        TreeSet<Integer> years = new TreeSet<>();

        enrollmentRepository.findAll().forEach(enrollment -> years.add(enrollment.getYear()));

        return new ArrayList<>(years.descendingSet());
    }

    /**
     * 卒業要件が設定済みのいずれかのカリキュラムで
     * 卒業可能な状態か判定する。
     */
    public boolean isGraduationPossible() throws SQLException {

        for (Curriculum curriculum : curriculumService.getAllCurricula()) {

            List<GraduationRequirement> requirements = graduationRequirementService.getRequirements(curriculum.getId());

            List<MandatoryCourseStatus> mandatoryCourses = graduationRequirementService.getMandatoryCourseStatuses(curriculum.getId());

            if (requirements.isEmpty() && mandatoryCourses.isEmpty()) {

                continue;
            }

            if (graduationRequirementService.isGraduated(curriculum.getId())) {

                return true;
            }
        }

        return false;
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
