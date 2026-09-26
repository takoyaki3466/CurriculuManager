package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.repository.CourseCategoryRepository;
import org.takoyaki.curriculummanager.service.abstracts.AbstractRepositoryService;

import java.util.List;

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
        requireId(curriculumId, "カリキュラムが指定されていません。");

        try {
            return categoryRepository.findByCurriculumId(curriculumId);
        } catch (Exception e) {
            throw new RuntimeException("カテゴリーの取得に失敗しました。", e);
        }
    }

    public List<CourseCategory> getAllCategories() {
        try {
            return categoryRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("カテゴリー一覧の取得に失敗しました。", e);
        }
    }

    public void addCategory(CourseCategory category) {
        requireEntity(category, "カテゴリーが指定されていません。");

        if (category.getCurriculumId() == null) {
            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        category.setName(requireText(category.getName(), "カテゴリー名を入力してください。"));

        try {
            categoryRepository.save(category);
        } catch (Exception e) {
            throw new RuntimeException("カテゴリーの追加に失敗しました。", e);
        }
    }

    public void updateCategory(CourseCategory category) {
        requireEntity(category, "更新するカテゴリーが指定されていません。");

        if (category.getId() == null) {
            throw new IllegalArgumentException("更新するカテゴリーが指定されていません。");
        }

        if (category.getCurriculumId() == null) {
            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        category.setName(requireText(category.getName(), "カテゴリー名を入力してください。"));

        try {
            categoryRepository.update(category);
        } catch (Exception e) {
            throw new RuntimeException("カテゴリーの更新に失敗しました。", e);
        }
    }

    public void deleteCategory(Integer id) {
        requireId(id, "削除するカテゴリーが指定されていません。");

        try {
            categoryRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("カテゴリーの削除に失敗しました。", e);
        }
    }
}
