package mx.uv.fei.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mx.uv.fei.domain.dto.Activity;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ActivityFormBuilder {

    @FXML
    private TextField nameTextField;
    @FXML
    private ChoiceBox<String> organizationChoiceBox;
    @FXML
    private ChoiceBox<String> managerChoiceBox;
    @FXML
    private TextField startDayTextField;
    @FXML
    private TextField startMonthTextField;
    @FXML
    private TextField startYearTextField;
    @FXML
    private TextField deadLineDayTextField;
    @FXML
    private TextField deadLineMonthTextField;
    @FXML
    private TextField deadLineYearTextField;
    @FXML
    private TextArea descriptionTextArea;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        organizationChoiceBox.getItems().addAll("Organización A", "Organización B", "Organización C");
        managerChoiceBox.getItems().addAll("Juan Pérez", "Ana Gómez", "Luis Martínez");

        saveButton.setOnAction(event -> handleSaveAction());
        cancelButton.setOnAction(event -> handleCancelAction());
    }

    @FXML
    private void handleSaveAction() {
        try {
            Activity activity = new Activity();

            activity.setName(nameTextField.getText());
            activity.setDescription(descriptionTextArea.getText());
            activity.setManager(managerChoiceBox.getValue());

            Date startDate = parseDate(startDayTextField.getText(), startMonthTextField.getText(),
                    startYearTextField.getText());
            Date endDate = parseDate(deadLineDayTextField.getText(), deadLineMonthTextField.getText(),
                    deadLineYearTextField.getText());

            activity.setStartDate(startDate);
            activity.setEndDate(endDate);

        } catch (IllegalArgumentException | DateTimeParseException e) {
            showErrorAlert("Error de fecha", "Por favor, introduzca fechas válidas en formato numérico (DD/MM/AAAA).");
        }
    }

    @FXML
    private void handleCancelAction() {
        nameTextField.clear();
        descriptionTextArea.clear();
        organizationChoiceBox.getSelectionModel().clearSelection();
        managerChoiceBox.getSelectionModel().clearSelection();
        startDayTextField.clear();
        startMonthTextField.clear();
        startYearTextField.clear();
        deadLineDayTextField.clear();
        deadLineMonthTextField.clear();
        deadLineYearTextField.clear();
    }

    private Date parseDate(String dayString, String monthString, String yearString)
            throws IllegalArgumentException, DateTimeParseException {
        if (dayString == null || dayString.isEmpty() || monthString == null || monthString.isEmpty()
                || yearString == null
                || yearString.isEmpty()) {
            throw new IllegalArgumentException("Campos de fecha vacíos");
        }

        int day = Integer.parseInt(dayString);
        int month = Integer.parseInt(monthString);
        int year = Integer.parseInt(yearString);

        LocalDate localDate = LocalDate.of(year, month, day);
        return Date.valueOf(localDate);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}