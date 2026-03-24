module mx.uv.fei {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens mx.uv.fei to javafx.fxml;
    exports mx.uv.fei;
}