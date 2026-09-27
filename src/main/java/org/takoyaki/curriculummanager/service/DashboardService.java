package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.i18n.I18n;
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

import static org.takoyaki.curriculummanager.util.NumberFormatUtils.formatCredits;

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
                            ? I18n.text("graduation.status.notConfigured")
                            : graduationRequirementService.areRequirementsSatisfied(curriculum.getId())
                            ? I18n.text("graduation.status.satisfied")
                            : I18n.text("graduation.status.unsatisfied");
                    String mandatoryStatus = mandatoryCourses.isEmpty()
                            ? I18n.text("graduation.status.notApplicable")
                            : graduationRequirementService.isMandatoryCoursesSatisfied(curriculum.getId())
                            ? I18n.text("graduation.status.satisfied")
                            : I18n.text("graduation.status.unsatisfied");
                    String curriculumStatus = graduationRequirementService.isGraduated(curriculum.getId())
                            ? I18n.text("graduation.status.possible")
                            : I18n.text("graduation.status.impossible");
                    lines.add("    " + I18n.text(
                            "dashboard.requirement.header",
                            curriculumStatus,
                            curriculum.getName(),
                            requirementStatus,
                            mandatoryStatus
                    ));

                    for (GraduationRequirement requirement : requirements) {
                        boolean satisfied = graduationRequirementService.getRemainingCredits(requirement) <= 0;
                        lines.add("      " + I18n.text(
                                "dashboard.requirement.line",
                                satisfied
                                        ? I18n.text("graduation.status.satisfied")
                                        : I18n.text("graduation.status.unsatisfied"),
                                requirement.getName(),
                                formatCredits(requirement.getRequiredCredits())
                        ));
                    }

                    for (MandatoryCourseStatus course : mandatoryCourses) {
                        lines.add("      " + I18n.text(
                                "dashboard.mandatory.line",
                                course.isPassed()
                                        ? I18n.text("graduation.status.satisfied")
                                        : I18n.text("graduation.status.unsatisfied"),
                                course.getCourseName(),
                                formatCredits(course.getCredits())
                        ));
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

}
