package mx.uv.fei.domain.common;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;

import java.util.Map;

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

    public static void showAlert(String title, String message, AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Platform.runLater(alert::show);
    }

    public static <T> void setupCheckBoxListView(ListView<T> listView, Map<T, BooleanProperty> selectionMap) {
        listView.setCellFactory(CheckBoxListCell.forListView(selectionMap::get));
        listView.setOnMouseClicked(_ -> toggleSelectedItem(listView, selectionMap));
    }

    private static <T> void toggleSelectedItem(ListView<T> listView, Map<T, BooleanProperty> selectionMap) {
        T selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            BooleanProperty checkboxState = selectionMap.get(selectedItem);
            if (checkboxState != null) {
                checkboxState.set(!checkboxState.get());
                listView.getSelectionModel().clearSelection();
            }
        }
    }
}