package mx.uv.fei.presentation;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.manager.ProjectManager;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class ProjectFormBuilder {

    private final ProjectManager projectManager;
    @FXML
    private TextField projectNameTextField;
    @FXML
    private ChoiceBox<String> organizationChoiceBox;
    @FXML
    private ChoiceBox<String> managerChoiceBox;
    @FXML
    private TextField capacityTextField;
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

    public ProjectFormBuilder(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @FXML
    public void initialize() {
        organizationChoiceBox.getItems().addAll("Organización A", "Organización B", "Organización C");
        managerChoiceBox.getItems().addAll("Juan Pérez", "Ana Gómez", "Luis Martínez");

        saveButton.setOnAction(actionEvent -> handleSaveAction());
        cancelButton.setOnAction(actionEvent -> handleCancelAction());
    }

    @FXML
    private void handleSaveAction() {
        try {
            Project projectInformation = new Project();

            projectInformation.setProjectName(projectNameTextField.getText());
            projectInformation.setDescription(descriptionTextArea.getText());
            projectInformation.setManager(managerChoiceBox.getValue());

            int projectCapacity = Integer.parseInt(capacityTextField.getText());
            projectInformation.setParticipantCapacity(projectCapacity);

            Date projectStartDate = parseDate(startDayTextField.getText(), startMonthTextField.getText(),
                    startYearTextField.getText());
            Date projectEndDate = parseDate(deadLineDayTextField.getText(), deadLineMonthTextField.getText(),
                    deadLineYearTextField.getText());

            projectInformation.setStartDate(projectStartDate);
            projectInformation.setEndDate(projectEndDate);

            boolean isActivitySavedSuccessfully = projectManager.registerNewProject(projectInformation);

            if (isActivitySavedSuccessfully) {
                handleCancelAction();
            }

        } catch (NumberFormatException capacityFormatMismatchException) {
            showErrorAlert("Error de formato", "El cupo de participantes debe ser un número entero válido.");
        } catch (IllegalArgumentException | DateTimeParseException dateValidationException) {
            showErrorAlert("Error de fecha", "Por favor, introduzca fechas válidas en formato numérico (DD/MM/AAAA).");
        } catch (DAOException e) {
            showErrorAlert("Error de coneccion", "Hubo un error en la coneccion, intentelo más tarde");
        }
    }

    @FXML
    private void handleCancelAction() {
        projectNameTextField.clear();
        descriptionTextArea.clear();
        capacityTextField.clear();
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
                || yearString == null || yearString.isEmpty()) {
            throw new IllegalArgumentException("Campos de fecha vacíos");
        }

        int day = Integer.parseInt(dayString);
        int month = Integer.parseInt(monthString);
        int year = Integer.parseInt(yearString);

        LocalDate localDate = LocalDate.of(year, month, day);
        return Date.valueOf(localDate);
    }

    private void showErrorAlert(String alertTitle, String alertMessage) {
        Alert userAlert = new Alert(Alert.AlertType.ERROR);
        userAlert.setTitle(alertTitle);
        userAlert.setHeaderText(null);
        userAlert.setContentText(alertMessage);
        userAlert.showAndWait();
    }
}
