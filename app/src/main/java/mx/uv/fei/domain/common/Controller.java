package mx.uv.fei.domain.common;

import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;

public class Controller {

    public static void showSuccessAlert(String alertTitle, String alertMessage) {
        showAlert(alertTitle, alertMessage, AlertType.INFORMATION);
    }

    public static void showInfoAlert(String alertTitle, String alertMessage) {
        showAlert(alertTitle, alertMessage, AlertType.INFORMATION);
    }

    public static void showErrorAlert(String alertTitle, String alertMessage) {
        showAlert(alertTitle, alertMessage, AlertType.ERROR);
    }

    public static void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Platform.runLater(alert::showAndWait);
    }

    public static <T> void setupCheckBoxListView(ListView<T> listView, Map<T, BooleanProperty> selectionMap) {
        listView.setCellFactory(CheckBoxListCell.forListView(selectionMap::get));

        listView.setOnMouseClicked(_ -> {
            T selectedItem = listView.getSelectionModel().getSelectedItem();

            if (selectedItem != null) {
                BooleanProperty checkboxState = selectionMap.get(selectedItem);

                if (checkboxState != null) {
                    checkboxState.set(!checkboxState.get());
                    listView.getSelectionModel().clearSelection();
                }
            }
        });
    }
}
