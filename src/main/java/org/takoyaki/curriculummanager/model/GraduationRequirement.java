package org.takoyaki.curriculummanager.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 卒業要件を表すモデルクラスです。
 *
 * <p>
 * 卒業要件は、
 * </p>
 *
 * <ul>
 *     <li>カリキュラム全体</li>
 *     <li>1つの科目カテゴリ</li>
 *     <li>複数の科目カテゴリ</li>
 * </ul>
 *
 * <p>
 * のいずれかを対象として必要単位数を設定できます。
 * </p>
 *
 * <p>
 * カテゴリIDが空の場合は「すべてのカテゴリ」を意味します。
 * </p>
 *
 * <p>
 * 例:
 * </p>
 *
 * <pre>
 * 全体       → 126単位
 *
 * 教養教育   → 16単位
 *
 * 数学・物理・化学
 *            → 合計14単位
 * </pre>
 */
public class GraduationRequirement {

    private Integer id;

    private Integer curriculumId;

    /**
     * この卒業要件の対象となるカテゴリID。
     *
     * <p>
     * 空の場合は全カテゴリを対象とします。
     * </p>
     */
    private List<Integer> categoryIds;

    private String name;

    private double requiredCredits;

    /**
     * 1カテゴリを対象とする既存形式のコンストラクタ。
     *
     * <p>
     * categoryIdがnullの場合は全体を対象とします。
     * </p>
     */
    public GraduationRequirement(Integer curriculumId, Integer categoryId, String name, double requiredCredits) {

        this.curriculumId = curriculumId;

        this.categoryIds = new ArrayList<>();

        if (categoryId != null) {
            this.categoryIds.add(categoryId);
        }

        this.name = name;
        this.requiredCredits = requiredCredits;
    }

    /**
     * 複数カテゴリを対象とするコンストラクタ。
     *
     * <p>
     * categoryIdsが空の場合は全体を対象とします。
     * </p>
     */
    public GraduationRequirement(Integer curriculumId, List<Integer> categoryIds, String name, double requiredCredits) {

        this.curriculumId = curriculumId;

        this.categoryIds = categoryIds == null ? new ArrayList<>() : new ArrayList<>(categoryIds);

        this.name = name;
        this.requiredCredits = requiredCredits;
    }

    /**
     * データベースから取得した卒業要件用のコンストラクタ。
     *
     * <p>
     * 既存のRepositoryとの互換性を維持するため、
     * 1カテゴリ形式をそのまま利用できます。
     * </p>
     */
    public GraduationRequirement(Integer id, Integer curriculumId, Integer categoryId, String name, double requiredCredits) {

        this.id = id;

        this.curriculumId = curriculumId;

        this.categoryIds = new ArrayList<>();

        if (categoryId != null) {
            this.categoryIds.add(categoryId);
        }

        this.name = name;
        this.requiredCredits = requiredCredits;
    }

    /**
     * 複数カテゴリを対象とするデータベース取得用コンストラクタ。
     */
    public GraduationRequirement(Integer id, Integer curriculumId, List<Integer> categoryIds, String name, double requiredCredits) {

        this.id = id;

        this.curriculumId = curriculumId;

        this.categoryIds = categoryIds == null ? new ArrayList<>() : new ArrayList<>(categoryIds);

        this.name = name;
        this.requiredCredits = requiredCredits;
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

    /**
     * 先頭のカテゴリIDを取得します。
     *
     * <p>
     * 既存コードとの互換性のために残しています。
     * </p>
     *
     * <p>
     * 複数カテゴリの場合は先頭のIDのみ返します。
     * 複数カテゴリを取得する場合は
     * {@link #getCategoryIds()} を使用してください。
     * </p>
     *
     * @return 先頭のカテゴリID。
     * 全体対象の場合はnull。
     */
    public Integer getCategoryId() {

        if (categoryIds == null || categoryIds.isEmpty()) {

            return null;
        }

        return categoryIds.get(0);
    }

    /**
     * 1つのカテゴリを設定します。
     *
     * <p>
     * nullを指定した場合は全体対象になります。
     * </p>
     */
    public void setCategoryId(Integer categoryId) {

        this.categoryIds = new ArrayList<>();

        if (categoryId != null) {
            this.categoryIds.add(categoryId);
        }
    }

    /**
     * 対象カテゴリID一覧を取得します。
     *
     * <p>
     * 返却されるリストを直接変更せず、
     * {@link #setCategoryIds(List)} を使用してください。
     * </p>
     */
    public List<Integer> getCategoryIds() {

        return new ArrayList<>(categoryIds);
    }

    /**
     * 対象カテゴリID一覧を設定します。
     *
     * <p>
     * nullまたは空のリストを指定すると
     * 全体対象になります。
     * </p>
     */
    public void setCategoryIds(List<Integer> categoryIds) {

        this.categoryIds = categoryIds == null ? new ArrayList<>() : new ArrayList<>(categoryIds);
    }

    /**
     * この卒業要件が全カテゴリを対象としているか判定します。
     *
     * @return 全体対象の場合true。
     */
    public boolean isAllCategories() {

        return categoryIds == null || categoryIds.isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRequiredCredits() {
        return requiredCredits;
    }

    public void setRequiredCredits(double requiredCredits) {
        this.requiredCredits = requiredCredits;
    }

    @Override
    public String toString() {
        return name;
    }
}