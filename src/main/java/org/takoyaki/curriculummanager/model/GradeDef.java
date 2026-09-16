package org.takoyaki.curriculummanager.model;

/**
 * 成績の定義を表すモデルクラスです。
 * <p>
 * 例:
 * <p>
 * S → 4.0
 * A → 3.0
 * B → 2.0
 * C → 1.0
 * D → 0.0
 * N → GPA対象外
 * <p>
 * 成績をenumで固定せず、データベースで管理することで
 * 大学ごとの異なる成績制度にも対応できます。
 */
public class GradeDef {
    private Integer id;
    private String symbol;
    private Double gpaPoints;
    private boolean passed;
    private boolean includedInGpa;

    public GradeDef(String symbol, Double gpaPoints, boolean passed, boolean includedInGpa) {
        this.symbol = symbol;
        this.gpaPoints = gpaPoints;
        this.passed = passed;
        this.includedInGpa = includedInGpa;
    }

    public GradeDef(Integer id, String symbol, Double gpaPoints, boolean passed, boolean includedInGpa) {
        this.id = id;
        this.symbol = symbol;
        this.gpaPoints = gpaPoints;
        this.passed = passed;
        this.includedInGpa = includedInGpa;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getGpaPoints() {
        return gpaPoints;
    }

    public void setGpaPoints(Double gpaPoints) {
        this.gpaPoints = gpaPoints;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public boolean isIncludedInGpa() {
        return includedInGpa;
    }

    public void setIncludedInGpa(boolean includedInGpa) {
        this.includedInGpa = includedInGpa;
    }

    @Override
    public String toString() {
        return symbol;
    }
}