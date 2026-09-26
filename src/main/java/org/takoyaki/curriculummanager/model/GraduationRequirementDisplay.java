package org.takoyaki.curriculummanager.model;

/**
 * 卒業要件画面に表示する情報。
 *
 * <p>
 * 卒業要件そのものを表す
 * {@link GraduationRequirement} とは異なり、
 * このクラスは画面表示専用です。
 * </p>
 *
 * <p>
 * 表示する情報:
 * </p>
 *
 * <ul>
 *     <li>要件名</li>
 *     <li>対象カテゴリ</li>
 *     <li>必要単位</li>
 *     <li>修得単位</li>
 *     <li>残り必要単位</li>
 *     <li>達成状況</li>
 * </ul>
 */
public class GraduationRequirementDisplay {

    private final Integer requirementId;

    /**
     * 卒業要件名。
     * <p>
     * 例:
     * 「卒業必要単位」
     * 「一般教養」
     * 「基礎科学分野」
     */
    private final String name;

    /**
     * 対象カテゴリの表示文字列。
     * <p>
     * 例:
     * <p>
     * 「すべて」
     * <p>
     * 「一般教養」
     * <p>
     * 「数学 / 物理 / 化学」
     */
    private final String targetCategories;

    /**
     * 必要単位数。
     */
    private final double requiredCredits;

    /**
     * 現在修得している単位数。
     */
    private final double earnedCredits;

    /**
     * 対象カテゴリを含むコンストラクタ。
     *
     * @param name             卒業要件名
     * @param targetCategories 対象カテゴリ表示
     * @param requiredCredits  必要単位数
     * @param earnedCredits    修得単位数
     */
    public GraduationRequirementDisplay(String name, String targetCategories, double requiredCredits, double earnedCredits) {

        this(null, name, targetCategories, requiredCredits, earnedCredits);
    }

    public GraduationRequirementDisplay(Integer requirementId, String name, String targetCategories, double requiredCredits, double earnedCredits) {

        this.requirementId = requirementId;

        this.name = name;

        this.targetCategories = targetCategories == null ? "" : targetCategories;

        this.requiredCredits = requiredCredits;

        this.earnedCredits = earnedCredits;
    }

    /**
     * 既存コードとの互換性用コンストラクタ。
     *
     * <p>
     * 対象カテゴリを指定しない場合、
     * targetCategoriesは空文字になります。
     * </p>
     *
     * <p>
     * 今後は基本的に4引数コンストラクタを
     * 使用します。
     * </p>
     *
     * @param name            卒業要件名
     * @param requiredCredits 必要単位数
     * @param earnedCredits   修得単位数
     */
    public GraduationRequirementDisplay(String name, double requiredCredits, double earnedCredits) {

        this(name, "", requiredCredits, earnedCredits);
    }

    /**
     * 卒業要件名を取得します。
     */
    public String getName() {

        return name;
    }

    public Integer getRequirementId() {

        return requirementId;
    }

    /**
     * 対象カテゴリの表示文字列を取得します。
     *
     * <p>
     * 例:
     * </p>
     *
     * <pre>
     * すべて
     *
     * 一般教養
     *
     * 数学 / 物理 / 化学
     * </pre>
     */
    public String getTargetCategories() {

        return targetCategories;
    }

    /**
     * 必要単位数を取得します。
     */
    public double getRequiredCredits() {

        return requiredCredits;
    }

    /**
     * 修得単位数を取得します。
     */
    public double getEarnedCredits() {

        return earnedCredits;
    }

    /**
     * 残り必要単位数を取得します。
     *
     * <p>
     * 必要単位を超えて修得している場合でも、
     * マイナスにはせず0を返します。
     * </p>
     */
    public double getRemainingCredits() {

        return Math.max(0.0, requiredCredits - earnedCredits);
    }

    /**
     * この卒業要件を達成しているか確認します。
     *
     * @return 必要単位以上を修得していればtrue
     */
    public boolean isSatisfied() {

        return earnedCredits >= requiredCredits;
    }

    /**
     * 達成状況を日本語で取得します。
     */
    public String getStatus() {

        return isSatisfied() ? "達成" : "未達成";
    }
}
