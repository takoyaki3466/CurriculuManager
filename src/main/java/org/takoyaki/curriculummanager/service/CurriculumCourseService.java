package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.Course;
import org.takoyaki.curriculummanager.model.CurriculumCourse;
import org.takoyaki.curriculummanager.model.CurriculumCourseDisplay;
import org.takoyaki.curriculummanager.repository.CourseRepository;
import org.takoyaki.curriculummanager.repository.CurriculumCourseRepository;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CurriculumCourseService {
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final CourseRepository courseRepository;

    public CurriculumCourseService() {
        curriculumCourseRepository = new CurriculumCourseRepository();
        courseRepository = new CourseRepository();
    }

    public List<CurriculumCourse> getCurriculumCourses(Integer curriculumId) throws SQLException {
        validateCurriculumId(curriculumId);
        return curriculumCourseRepository.findByCurriculumId(curriculumId);
    }

    public List<Integer> getCourseIds(Integer curriculumId) throws SQLException {
        return getCurriculumCourses(curriculumId).stream().map(CurriculumCourse::getCourseId).filter(id -> id != null).toList();
    }

    public List<Course> getCourses(Integer curriculumId) throws SQLException {
        List<Integer> courseIds = getCourseIds(curriculumId);
        List<Course> courses = new ArrayList<>();

        for (Integer courseId : courseIds) {
            Course course = courseRepository.findById(courseId);

            if (course != null) {
                courses.add(course);
            }
        }

        return courses;
    }

    public List<CurriculumCourseDisplay> getCourseDisplaysByCategory(Integer curriculumId, Integer categoryId) throws SQLException {
        validateCurriculumId(curriculumId);

        if (categoryId == null) {
            throw new IllegalArgumentException("カテゴリIDが指定されていません。");
        }

        if (categoryId <= 0) {
            throw new IllegalArgumentException("カテゴリIDが不正です。");
        }

        List<Course> courses = courseRepository.findByCategoryId(categoryId);
        List<CurriculumCourse> curriculumCourses = curriculumCourseRepository.findByCurriculumId(curriculumId);
        List<CurriculumCourseDisplay> displays = new ArrayList<>();

        for (Course course : courses) {
            if (course == null) {
                continue;
            }

            CurriculumCourse relation = findRelation(curriculumCourses, course.getId());
            String requirementType = null;
            Integer relationId = null;

            if (relation != null) {
                relationId = relation.getId();
                requirementType = relation.getRequirementType();
            }

            displays.add(new CurriculumCourseDisplay(relationId, curriculumId, course.getId(), course.getCourseCode(), course.getName(), course.getCredits(), course.getCategoryId(), requirementType));
        }

        return displays;
    }

    private CurriculumCourse findRelation(List<CurriculumCourse> curriculumCourses, Integer courseId) {
        if (courseId == null) {
            return null;
        }

        for (CurriculumCourse curriculumCourse : curriculumCourses) {
            if (curriculumCourse == null) {
                continue;
            }

            if (courseId.equals(curriculumCourse.getCourseId())) {
                return curriculumCourse;
            }
        }

        return null;
    }

    public boolean exists(Integer curriculumId, Integer courseId) throws SQLException {
        validateCurriculumId(curriculumId);
        validateCourseId(courseId);
        return curriculumCourseRepository.findByCurriculumIdAndCourseId(curriculumId, courseId) != null;
    }

    public void addCourseToCurriculum(Integer curriculumId, Integer courseId, String requirementType) throws SQLException {
        validateCurriculumId(curriculumId);
        validateCourseId(courseId);
        validateRequirementType(requirementType);
        Course course = courseRepository.findById(courseId);

        if (course == null) {
            throw new IllegalArgumentException("科目が存在しません: " + courseId);
        }

        if (exists(curriculumId, courseId)) {
            throw new IllegalArgumentException("この科目は既にカリキュラムへ登録されています。");
        }

        CurriculumCourse curriculumCourse = new CurriculumCourse(curriculumId, courseId, requirementType);
        curriculumCourseRepository.save(curriculumCourse);
    }

    public void update(CurriculumCourse curriculumCourse) throws SQLException {
        if (curriculumCourse == null) {
            throw new IllegalArgumentException("カリキュラム科目情報がnullです。");
        }

        if (curriculumCourse.getId() == null) {
            throw new IllegalArgumentException("更新対象のIDが指定されていません。");
        }

        validateCurriculumId(curriculumCourse.getCurriculumId());
        validateCourseId(curriculumCourse.getCourseId());
        validateRequirementType(curriculumCourse.getRequirementType());
        curriculumCourseRepository.update(curriculumCourse);
    }

    public void removeCourseFromCurriculum(Integer curriculumId, Integer courseId) throws SQLException {
        validateCurriculumId(curriculumId);
        validateCourseId(courseId);

        if (!exists(curriculumId, courseId)) {
            throw new IllegalArgumentException("この科目はカリキュラムに登録されていません。");
        }

        curriculumCourseRepository.deleteByCurriculumIdAndCourseId(curriculumId, courseId);
    }

    public void delete(Integer id) throws SQLException {
        if (id == null) {
            throw new IllegalArgumentException("削除対象のIDが指定されていません。");
        }

        if (curriculumCourseRepository.findById(id) == null) {
            throw new IllegalArgumentException("カリキュラム科目情報が存在しません: " + id);
        }

        curriculumCourseRepository.deleteById(id);
    }

    private void validateRequirementType(String requirementType) {
        if (requirementType == null || requirementType.isBlank()) {
            throw new IllegalArgumentException("要件種別を入力してください。");
        }
    }

    private void validateCurriculumId(Integer curriculumId) {
        if (curriculumId == null) {
            throw new IllegalArgumentException("カリキュラムIDが指定されていません。");
        }

        if (curriculumId <= 0) {
            throw new IllegalArgumentException("カリキュラムIDが不正です。");
        }
    }

    private void validateCourseId(Integer courseId) {
        if (courseId == null) {
            throw new IllegalArgumentException("科目IDが指定されていません。");
        }

        if (courseId <= 0) {
            throw new IllegalArgumentException("科目IDが不正です。");
        }
    }
}
