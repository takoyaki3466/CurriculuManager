package org.takoyaki.curriculummanager.service;

import org.takoyaki.curriculummanager.model.CourseCategory;
import org.takoyaki.curriculummanager.repository.CourseCategoryRepository;

import java.util.List;

/**
 * 科目カテゴリーに関する処理を担当するServiceです。
 *
 * <p>
 * Controllerから直接Repositoryを操作せず、
 * カテゴリーに関する業務処理をこのクラスにまとめます。
 * </p>
 */
public class CourseCategoryService {

    /**
     * 科目カテゴリーを操作するRepositoryです。
     */
    private final CourseCategoryRepository repository;

    /**
     * Serviceを生成します。
     */
    public CourseCategoryService() {
        this.repository = new CourseCategoryRepository();
    }

    /**
     * 指定したカリキュラムのカテゴリーを取得します。
     *
     * @param curriculumId カリキュラムID
     * @return カテゴリー一覧
     */
    public List<CourseCategory> getCategories(Integer curriculumId) {

        if (curriculumId == null) {
            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        try {
            return repository.findByCurriculumId(curriculumId);

        } catch (Exception e) {
            throw new RuntimeException("カテゴリーの取得に失敗しました。", e);
        }
    }

    /**
     * すべてのカリキュラムのカテゴリーを取得します。
     */
    public List<CourseCategory> getAllCategories() {

        try {

            return repository.findAll();

        } catch (Exception e) {

            throw new RuntimeException("カテゴリー一覧の取得に失敗しました。", e);
        }
    }

    /**
     * カテゴリーを新規登録します。
     *
     * @param category 登録するカテゴリー
     */
    public void addCategory(CourseCategory category) {

        if (category == null) {
            throw new IllegalArgumentException("カテゴリーが指定されていません。");
        }

        if (category.getCurriculumId() == null) {
            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        if (category.getName() == null || category.getName().isBlank()) {

            throw new IllegalArgumentException("カテゴリー名を入力してください。");
        }

        try {
            repository.save(category);

        } catch (Exception e) {
            throw new RuntimeException("カテゴリーの追加に失敗しました。", e);
        }
    }

    /**
     * カテゴリーを更新します。
     *
     * @param category 更新するカテゴリー
     */
    public void updateCategory(CourseCategory category) {

        if (category == null || category.getId() == null) {

            throw new IllegalArgumentException("更新するカテゴリーが指定されていません。");
        }

        if (category.getCurriculumId() == null) {
            throw new IllegalArgumentException("カリキュラムが指定されていません。");
        }

        if (category.getName() == null || category.getName().isBlank()) {

            throw new IllegalArgumentException("カテゴリー名を入力してください。");
        }

        try {
            repository.update(category);

        } catch (Exception e) {
            throw new RuntimeException("カテゴリーの更新に失敗しました。", e);
        }
    }

    /**
     * カテゴリーを削除します。
     *
     * <p>
     * データベース側の外部キー制約により、
     * 子カテゴリーも削除されます。
     * </p>
     *
     * <p>
     * また、そのカテゴリーに所属していた科目は
     * カテゴリーIDがNULLになります。
     * </p>
     *
     * @param id 削除するカテゴリーID
     */
    public void deleteCategory(Integer id) {

        if (id == null) {
            throw new IllegalArgumentException("削除するカテゴリーが指定されていません。");
        }

        try {
            repository.deleteById(id);

        } catch (Exception e) {
            throw new RuntimeException("カテゴリーの削除に失敗しました。", e);
        }
    }
}
