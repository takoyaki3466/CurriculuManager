package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Curriculum;
import org.takoyaki.curriculummanager.model.Department;
import org.takoyaki.curriculummanager.model.Enrollment;
import org.takoyaki.curriculummanager.model.GraduationRequirement;
import org.takoyaki.curriculummanager.model.Major;
import org.takoyaki.curriculummanager.model.MandatoryCourseStatus;
import org.takoyaki.curriculummanager.repository.EnrollmentRepository;
import org.takoyaki.curriculummanager.service.abstracts.AcademicContextService;
import org.takoyaki.curriculummanager.service.interfaces.AcademicContextProvider;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DashboardService {
    private final CurriculumService curriculumService;
    private final GraduationRequirementService graduationRequirementService;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicContextProvider academicContextService;

    public DashboardService() {
        curriculumService = new CurriculumService();
        graduationRequirementService = new GraduationRequirementService();
        enrollmentRepository = new EnrollmentRepository();
        academicContextService = new AcademicContextService();
    }

    public List<String> getCurriculumRequirementList() throws SQLException {
        List<String> lines = new ArrayList<>();
        List<Department> departments = curriculumService.getDepartments();
        Department currentDepartment = academicContextService.getCurrentDepartment();
        Major currentMajor = academicContextService.getCurrentMajor();

        for (Department department : departments) {
            if (currentDepartment == null || !department.getId().equals(currentDepartment.getId())) {
                continue;
            }

            lines.add(department.getName());
            List<Major> majors = curriculumService.getAllMajors().stream()
                    .filter(major -> department.getId().equals(major.getDepartmentId()))
                    .filter(major -> currentMajor != null && major.getId().equals(currentMajor.getId()))
                    .toList();

            for (Major major : majors) {
                lines.add("  " + major.getName());
                List<Curriculum> curricula = curriculumService.getCurricula(major.getId());

                for (Curriculum curriculum : curricula) {
                    List<GraduationRequirement> requirements = graduationRequirementService.getRequirements(curriculum.getId());
                    List<MandatoryCourseStatus> mandatoryCourses = graduationRequirementService.getMandatoryCourseStatuses(curriculum.getId());
                    String requirementStatus = requirements.isEmpty()
                            ? "未設定"
                            : graduationRequirementService.areRequirementsSatisfied(curriculum.getId()) ? "達成" : "未達成";
                    String mandatoryStatus = mandatoryCourses.isEmpty()
                            ? "対象なし"
                            : graduationRequirementService.isMandatoryCoursesSatisfied(curriculum.getId()) ? "達成" : "未達成";
                    String curriculumStatus = graduationRequirementService.isGraduated(curriculum.getId()) ? "卒業可能" : "卒業不可";
                    lines.add("    " + curriculumStatus + "  " + curriculum.getName()
                            + "（単位要件: " + requirementStatus
                            + " / 必修科目: " + mandatoryStatus + "）");

                    for (GraduationRequirement requirement : requirements) {
                        boolean satisfied = graduationRequirementService.getRemainingCredits(requirement) <= 0;
                        lines.add("      " + (satisfied ? "達成    " : "未達成  ")
                                + requirement.getName() + " " + formatCredits(requirement.getRequiredCredits()) + "単位");
                    }

                    for (MandatoryCourseStatus course : mandatoryCourses) {
                        lines.add("      " + (course.isPassed() ? "達成    " : "未達成  ")
                                + course.getCourseName() + " " + formatCredits(course.getCredits()) + "単位 必修");
                    }
                }
            }
        }

        return lines;
    }

    public List<Integer> getEnrollmentYears() throws SQLException {
        TreeSet<Integer> years = new TreeSet<>();
        getCurrentEnrollments().forEach(enrollment -> years.add(enrollment.getYear()));
        return new ArrayList<>(years.descendingSet());
    }

    public List<Enrollment> getCurrentEnrollments() {
        Major major = academicContextService.getCurrentMajor();
        List<Enrollment> enrollments = new ArrayList<>();

        if (major == null) {
            return enrollments;
        }

        for (Curriculum curriculum : curriculumService.getCurricula(major.getId())) {
            enrollments.addAll(enrollmentRepository.findByCurriculumId(curriculum.getId()));
        }

        return enrollments;
    }

    public boolean isGraduationPossible() throws SQLException {
        for (Curriculum curriculum : curriculumService.getAllCurricula()) {
            Major currentMajor = academicContextService.getCurrentMajor();

            if (currentMajor == null || !currentMajor.getId().equals(curriculum.getMajorId())) {
                continue;
            }

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

    private String formatCredits(double credits) {
        if (credits == Math.floor(credits)) {
            return String.valueOf((int) credits);
        }

        return String.valueOf(credits);
    }
}
