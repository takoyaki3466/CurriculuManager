package org.takoyaki.curriculummanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import org.takoyaki.curriculummanager.model.YearlyAcademicSummary;
import org.takoyaki.curriculummanager.service.CreditCalculationService;
import org.takoyaki.curriculummanager.service.DashboardService;
import org.takoyaki.curriculummanager.service.GpaCalculationService;

import java.sql.SQLException;
import java.util.List;

/**
 * ダッシュボード画面を管理するController。
 */
public class DashboardController {

    @FXML
    private Label gpaLabel;

    @FXML
    private Label earnedCreditsLabel;

    @FXML
    private Label graduationPossibleLabel;

    @FXML
    private TableView<YearlyAcademicSummary> yearlySummaryTable;

    @FXML
    private TableColumn<YearlyAcademicSummary, Number> yearColumn;

    @FXML
    private TableColumn<YearlyAcademicSummary, String> yearlyGpaColumn;

    @FXML
    private TableColumn<YearlyAcademicSummary, String> yearlyCreditsColumn;

    /**
     * 学科・カリキュラム・卒業要件一覧。
     */
    @FXML
    private ListView<String> curriculumRequirementListView;

    private final GpaCalculationService gpaCalculationService;

    private final CreditCalculationService creditCalculationService;

    private final DashboardService dashboardService;

    public DashboardController() {

        this.gpaCalculationService = new GpaCalculationService();

        this.creditCalculationService = new CreditCalculationService();

        this.dashboardService = new DashboardService();
    }

    /**
     * 画面初期化時に情報を読み込む。
     */
    @FXML
    private void initialize() throws SQLException {

        setupYearlySummaryTable();

        refresh();
    }

    private void setupYearlySummaryTable() {

        yearColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getYear()));

        yearlyGpaColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", data.getValue().getGpa())));

        yearlyCreditsColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("%.1f 単位", data.getValue().getEarnedCredits())));
    }

    /**
     * GPA・修得単位・
     * カリキュラム情報を再読み込みする。
     */
    @FXML
    private void refresh() throws SQLException {

        /*
         * GPAを計算する。
         */
        double gpa = gpaCalculationService.calculateGpa();

        /*
         * 修得単位を計算する。
         */
        double earnedCredits = creditCalculationService.calculateEarnedCredits();

        /*
         * GPAを表示する。
         */
        gpaLabel.setText(String.format("%.2f", gpa));

        /*
         * 修得単位を表示する。
         */
        earnedCreditsLabel.setText(String.format("%.1f 単位", earnedCredits));

        graduationPossibleLabel.setText(dashboardService.isGraduationPossible() ? "卒業可能" : "卒業不可");

        List<YearlyAcademicSummary> yearlySummaries = new java.util.ArrayList<>();

        for (Integer year : dashboardService.getEnrollmentYears()) {

            yearlySummaries.add(new YearlyAcademicSummary(

                    year,

                    gpaCalculationService.calculateGpaByYear(year),

                    creditCalculationService.calculateEarnedCreditsByYear(year)));
        }

        yearlySummaryTable.setItems(FXCollections.observableArrayList(yearlySummaries));

        /*
         * カリキュラム・卒業要件一覧を取得する。
         */
        List<String> curriculumRequirements = dashboardService.getCurriculumRequirementList();

        /*
         * ListViewへ設定する。
         */
        curriculumRequirementListView.setItems(FXCollections.observableArrayList(curriculumRequirements));
    }
}
