module org.takoyaki.curriculummanager {

    requires javafx.controls;
    requires javafx.fxml;

    requires org.xerial.sqlitejdbc;

    exports org.takoyaki.curriculummanager;

    opens org.takoyaki.curriculummanager to javafx.fxml;
    opens org.takoyaki.curriculummanager.controller to javafx.fxml;
    opens org.takoyaki.curriculummanager.model to javafx.base;
}
