package org.takoyaki.curriculummanager.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import org.takoyaki.curriculummanager.view.ViewManager;

/**
 * アプリケーション全体のメイン画面を管理するController。
 *
 * <p>
 * 実際の各画面の処理は担当せず、
 * ViewManagerを使って画面を切り替える。
 * </p>
 */
public class MainController {
    @FXML
    private StackPane contentPane;

    private ViewManager viewManager;

    /**
     * FXML読み込み後に呼び出される。
     */
    @FXML
    private void initialize() {

        viewManager = new ViewManager(contentPane);

        /*
         * 最初はダッシュボードを表示する。
         */
        viewManager.showDashboard();
    }

    /**
     * ダッシュボードを表示する。
     */
    @FXML
    private void openDashboard() {

        viewManager.showDashboard();
    }

    /**
     * 履修科目画面を表示する。
     */
    @FXML
    private void openEnrollment() {

        viewManager.showEnrollment();
    }

    /**
     * 成績画面を表示する。
     */
    @FXML
    private void openGrade() {

        viewManager.showGrade();
    }

    /**
     * カリキュラム画面を表示する。
     */
    @FXML
    private void openCurriculum() {

        viewManager.showCurriculum();
    }

    /**
     * 卒業要件画面を表示する。
     */
    @FXML
    private void openGraduation() {

        viewManager.showGraduation();
    }

}
