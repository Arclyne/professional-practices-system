package mx.uv.fei.domain.common;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);

    private static final String FILE_UNAVAILABLE_MESSAGE =
            "El archivo no está disponible.";

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

    public static void openStoredFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            showErrorAlert("Archivo no disponible", "El documento seleccionado no tiene un archivo asociado.");
        } else if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            showErrorAlert("Acción no disponible", "El sistema no permite abrir archivos desde la aplicación.");
        } else {
            browseStoredFile(fileUrl);
        }
    }

    public static void openStoredFilePath(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            showErrorAlert("Archivo no disponible", "El documento seleccionado no tiene un archivo asociado.");
        } else {
            openStoredFile(new File(filePath).toURI().toString());
        }
    }

    private static void browseStoredFile(String fileUrl) {
        try {
            URI fileUri = new URI(fileUrl);
            if (isMissingLocalFile(fileUri)) {
                showErrorAlert("Archivo no disponible", FILE_UNAVAILABLE_MESSAGE);
            } else {
                Desktop.getDesktop().browse(fileUri);
            }
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            log.error("No se pudo abrir el recurso almacenado: {}", fileUrl, e);
            showErrorAlert("Archivo no disponible", FILE_UNAVAILABLE_MESSAGE);
        }
    }

    private static boolean isMissingLocalFile(URI fileUri) {
        return !fileUri.isAbsolute()
                || ("file".equalsIgnoreCase(fileUri.getScheme()) && !Files.exists(Paths.get(fileUri)));
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

    public static String getRootError(Throwable e){
        String messageError = "Error desconocido: Excepción nula.";
        Throwable rootError = e;
        if(e != null) {
            while (rootError != null && rootError.getCause() != null) {
                rootError = rootError.getCause();
            }

            messageError = rootError.getMessage();
            if (messageError == null || messageError.trim().isEmpty()) {
                messageError = "Error interno: " + rootError.getClass().getSimpleName();
            }
        }
        return messageError;
    }
}