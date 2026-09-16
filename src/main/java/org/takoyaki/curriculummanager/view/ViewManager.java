package org.takoyaki.curriculummanager.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.takoyaki.curriculummanager.HelloApplication;

import java.io.IOException;

/**
 * アプリケーション内の画面切り替えを管理するクラス。
 *
 * <p>
 * Controller自身がFXMLを読み込むのではなく、
 * ViewManagerに画面切り替えを任せることで、
 * Controllerの責務を小さくする。
 * </p>
 */
public class ViewManager {
    private final StackPane contentPane;

    /**
     * 画面を表示する領域を受け取ります。
     *
     * @param contentPane 画面を差し替えるStackPane
     */
    public ViewManager(StackPane contentPane) {
        this.contentPane = contentPane;
    }

    /**
     * 指定したFXMLを画面に表示します。
     *
     * @param fxmlFile FXMLファイル名
     */
    public void show(String fxmlFile) {

        try {

            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));

            Node view = loader.load();

            contentPane.getChildren().clear();

            contentPane.getChildren().add(view);

        } catch (IOException | NullPointerException e) {

            throw new RuntimeException("画面を読み込めませんでした: " + fxmlFile, e);
        }
    }

    /**
     * ダッシュボードを表示します。
     */
    public void showDashboard() {
        show("dashboard-view.fxml");
    }

    /**
     * 履修科目画面を表示します。
     */
    public void showEnrollment() {

        show("enrollment-view.fxml");
    }

    /**
     * 成績画面を表示します。
     */
    public void showGrade() {
        show("grade-view.fxml");
    }

    /**
     * カリキュラム画面を表示します。
     */
    public void showCurriculum() {
        show("curriculum-view.fxml");
    }

    /**
     * 卒業要件画面を表示します。
     */
    public void showGraduation() {
        show("graduation-view.fxml");
    }

    /**
     * カリキュラム科目設定画面を表示します。
     *
     * <p>
     * カリキュラムにどの科目を登録するか、
     * また、その科目を「必修」「選択」の
     * どちらとして扱うかを設定する画面です。
     * </p>
     */
    public void showCurriculumCourse() {
        show("curriculum-course-view.fxml");
    }
}
