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
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class RegisterProjectController implements Initializable {

    private final ProjectManager projectManager;

    @FXML
    private FormField projectNameTextField;
    @FXML
    private FormComboBox organizationChoiceBox;
    @FXML
    private FormComboBox managerChoiceBox;
    @FXML
    private FormField capacityTextField;
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

    public RegisterProjectController(ProjectManager projectManager) {
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
            Project projectInformation = new Project();

            projectInformation.setProjectName(projectNameTextField.getText());
            projectInformation.setDescription(descriptionTextArea.getText());
            projectInformation.setManager((String) managerChoiceBox.getValue());

            int parsedProjectCapacity = Integer.parseInt(capacityTextField.getText());
            projectInformation.setParticipantCapacity(parsedProjectCapacity);

            Date projectStartDate = parseDate(startDayTextField.getText(), startMonthTextField.getText(),
                    startYearTextField.getText());
            Date projectEndDate = parseDate(deadLineDayTextField.getText(), deadLineMonthTextField.getText(),
                    deadLineYearTextField.getText());

            projectInformation.setStartDate(projectStartDate);
            projectInformation.setEndDate(projectEndDate);
            projectInformation.setStatus("Activo");
            projectInformation.setCompanyId(1);

            boolean isProjectSavedSuccessfully = projectManager.registerNewProject(projectInformation);

            if (isProjectSavedSuccessfully) {
                showErrorAlert("exceptions", "exito");
                closeCurrentWindow(actionEvent);
            }

        } catch (NumberFormatException capacityFormatMismatchException) {
            showErrorAlert("Error de formato", "El cupo de participantes debe ser un número entero válido.");
        } catch (IllegalArgumentException | DateTimeParseException dateValidationException) {
            showErrorAlert("Error de fecha", "Por favor, introduzca fechas válidas en formato numérico (DD/MM/AAAA).");
        } catch (DAOException databaseConnectionException) {
            showErrorAlert("Error de conexión", "Hubo un error en la conexión, inténtelo más tarde.");
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

        if (dayString == null || monthString == null || yearString == null) {
            throw new IllegalArgumentException("Campos de fecha nulos");
        }

        String day = dayString.trim();
        String month = monthString.trim();
        String year = yearString.trim();

        if (day.isEmpty() || month.isEmpty() || year.isEmpty()) {
            throw new IllegalArgumentException("Campos de fecha vacíos");
        }

        try {
            int parsedDay = Integer.parseInt(day);
            int parsedMonth = Integer.parseInt(month);
            int parsedYear = Integer.parseInt(year);

            LocalDate convertedLocalDate = LocalDate.of(parsedYear, parsedMonth, parsedDay);
            return Date.valueOf(convertedLocalDate);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Los valores de fecha deben ser numéricos");
        }
    }

    private void showErrorAlert(String alertTitle, String alertMessage) {
        Alert userAlert = new Alert(Alert.AlertType.ERROR);
        userAlert.setTitle(alertTitle);
        userAlert.setHeaderText(null);
        userAlert.setContentText(alertMessage);
        userAlert.showAndWait();
    }
}