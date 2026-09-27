package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.i18n.I18n;
import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.repository.CourseCategoryRepository;
import org.takoyaki.curriculummanager.service.abstracts.AbstractRepositoryService;

import java.util.List;

import static org.takoyaki.curriculummanager.util.ValidationUtils.requireEntity;
import static org.takoyaki.curriculummanager.util.ValidationUtils.requireId;
import static org.takoyaki.curriculummanager.util.ValidationUtils.requireText;

public class CourseCategoryService extends AbstractRepositoryService<CourseCategory> {
    private final CourseCategoryRepository categoryRepository;

    public CourseCategoryService() {
        this(new CourseCategoryRepository());
    }

    private CourseCategoryService(CourseCategoryRepository repository) {
        super(repository);
        this.categoryRepository = repository;
    }

    public List<CourseCategory> getCategories(Integer curriculumId) {
        requireId(curriculumId, I18n.text("validation.curriculum.required"));

        try {
            return categoryRepository.findByCurriculumId(curriculumId);
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.category.load"), e);
        }
    }

    public List<CourseCategory> getAllCategories() {
        try {
            return categoryRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.category.list"), e);
        }
    }

    public void addCategory(CourseCategory category) {
        requireEntity(category, I18n.text("validation.category.labelRequired"));

        if (category.getCurriculumId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.required"));
        }

        category.setName(requireText(category.getName(), I18n.text("validation.category.labelName")));

        try {
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.category.add"), e);
        }
    }

    public void updateCategory(CourseCategory category) {
        requireEntity(category, I18n.text("validation.category.labelUpdateRequired"));

        if (category.getId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.category.labelUpdateRequired"));
        }

        if (category.getCurriculumId() == null) {
            throw new IllegalArgumentException(I18n.text("validation.curriculum.required"));
        }

        category.setName(requireText(category.getName(), I18n.text("validation.category.labelName")));

        try {
            categoryRepository.update(category);
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.category.update"), e);
        }
    }

    public void deleteCategory(Integer id) {
        requireId(id, I18n.text("validation.category.labelDeleteRequired"));

        try {
            categoryRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException(I18n.text("service.category.delete"), e);
        }
    }
}
