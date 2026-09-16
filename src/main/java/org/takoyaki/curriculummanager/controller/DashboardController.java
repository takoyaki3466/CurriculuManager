package org.takoyaki.curriculummanager.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

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

        refresh();
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
