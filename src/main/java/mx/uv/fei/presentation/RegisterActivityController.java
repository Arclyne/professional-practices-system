package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class RegisterActivityController implements Initializable {

    private final ProjectManager projectManager;

    @FXML
    private FormField nameTextField;
    @FXML
    private FormComboBox organizationChoiceBox;
    @FXML
    private FormComboBox managerChoiceBox;
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

    public RegisterActivityController(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
        ObservableList<String> organizationOptions = FXCollections.observableArrayList(
                "Organización A", "Organización B", "Organización C");
        organizationChoiceBox.setItems(organizationOptions);

        ObservableList<String> managerOptions = FXCollections.observableArrayList(
                "Juan Pérez", "Ana Gómez", "Luis Martínez");
        managerChoiceBox.setItems(managerOptions);
    }

    @FXML
    private void handleSaveAction(ActionEvent actionEvent) {
        try {
            Activity activityInformation = new Activity();

            activityInformation.setName(nameTextField.getText());
            activityInformation.setDescription(descriptionTextArea.getText());
            activityInformation.setManager((String) managerChoiceBox.getValue());

            Date activityStartDate = parseDate(startDayTextField.getText(), startMonthTextField.getText(),
                    startYearTextField.getText());
            Date activityEndDate = parseDate(deadLineDayTextField.getText(), deadLineMonthTextField.getText(),
                    deadLineYearTextField.getText());

            activityInformation.setStartDate(activityStartDate);
            activityInformation.setEndDate(activityEndDate);

            boolean isActivitySavedSuccessfully = projectManager.registerNewActivity(activityInformation);

            if (isActivitySavedSuccessfully) {
                showErrorAlert("exceptions", "exito");
                closeCurrentWindow(actionEvent);
            }
        } catch (IllegalArgumentException | DateTimeParseException dateValidationException) {
            showErrorAlert("Error de fecha", "Por favor, introduzca fechas válidas en formato numérico (DD/MM/AAAA).");
        } catch (DAOException databaseConnectionException) {
            showErrorAlert("Fallo en la conexión", "Fallo de conexión, inténtelo más tarde.");
        }
    }

    @FXML
    private void handleCancelAction(ActionEvent actionEvent) {
        closeCurrentWindow(actionEvent);
    }

    private void closeCurrentWindow(ActionEvent actionEvent) {
        Node eventSourceNode = (Node) actionEvent.getSource();
        Stage currentStage = (Stage) eventSourceNode.getScene().getWindow();
        currentStage.close();
    }

    private Date parseDate(String dayString, String monthString, String yearString)
            throws IllegalArgumentException, DateTimeParseException {
        if (dayString == null || dayString.isEmpty() || monthString == null || monthString.isEmpty()
                || yearString == null || yearString.isEmpty()) {
            throw new IllegalArgumentException("Campos de fecha vacíos");
        }

        int parsedDay = Integer.parseInt(dayString);
        int parsedMonth = Integer.parseInt(monthString);
        int parsedYear = Integer.parseInt(yearString);

        LocalDate convertedLocalDate = LocalDate.of(parsedYear, parsedMonth, parsedDay);
        return Date.valueOf(convertedLocalDate);
    }

    private void showErrorAlert(String alertTitle, String alertMessage) {
        Alert userAlert = new Alert(Alert.AlertType.ERROR);
        userAlert.setTitle(alertTitle);
        userAlert.setHeaderText(null);
        userAlert.setContentText(alertMessage);
        userAlert.showAndWait();
    }
}