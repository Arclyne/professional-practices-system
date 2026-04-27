package mx.uv.fei.domain.common;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class CommonControler {

    public static void showSuccessAlert(String alertTitle, String alertMessage) {
        Alert userAlert = new Alert(Alert.AlertType.INFORMATION);
        userAlert.setTitle(alertTitle);
        userAlert.setHeaderText(null);
        userAlert.setContentText(alertMessage);
        userAlert.showAndWait();
    }

    public static void showInfoAlert(String alertTitle, String alertMessage) {
        Alert userAlert = new Alert(Alert.AlertType.INFORMATION);
        userAlert.setTitle(alertTitle);
        userAlert.setHeaderText(null);
        userAlert.setContentText(alertMessage);
        userAlert.showAndWait();
    }

    public static void showErrorAlert(String alertTitle, String alertMessage) {
        Alert userAlert = new Alert(Alert.AlertType.ERROR);
        userAlert.setTitle(alertTitle);
        userAlert.setHeaderText(null);
        userAlert.setContentText(alertMessage);
        userAlert.showAndWait();
    }

    public static void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
