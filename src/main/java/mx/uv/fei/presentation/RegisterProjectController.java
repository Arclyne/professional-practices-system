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

import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.domain.common.CommonParse;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

public class RegisterProjectController implements Initializable {

    private final ProjectManager projectManager;

    @FXML private FormField fieldProjectName;
    @FXML private FormField fieldCapacity;
    @FXML private FormComboBox comboBoxOrganization;
    @FXML private FormComboBox comboBoxManager;

    @FXML private TextField textFieldStartDay;
    @FXML private TextField textFieldStartMonth;
    @FXML private TextField textFieldStartYear;

    @FXML private TextField textFieldDeadlineDay;
    @FXML private TextField textFieldDeadlineMonth;
    @FXML private TextField textFieldDeadlineYear;

    @FXML private TextArea textAreaDescription;

    @FXML private Button buttonSave;
    @FXML private Button buttonCancel;

    public RegisterProjectController(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
        ObservableList<String> organizationOptions = FXCollections.observableArrayList(
                "Organización A", "Organización B", "Organización C");
        comboBoxOrganization.setItems(organizationOptions);

        ObservableList<String> managerOptions = FXCollections.observableArrayList(
                "Juan Pérez", "Ana Gómez", "Luis Martínez");

        comboBoxManager.setItems(managerOptions);
    }

    @FXML
    private void handleSaveAction(ActionEvent actionEvent) {
        try {
            Project projectInformation = new Project();

            projectInformation.setProjectName(fieldProjectName.getText());
            projectInformation.setDescription(textAreaDescription.getText());
            projectInformation.setManager(comboBoxManager.getValue());

            int parsedProjectCapacity = Integer.parseInt(fieldCapacity.getText());
            projectInformation.setParticipantCapacity(parsedProjectCapacity);

            Date projectStartDate = CommonParse.parseDate(
                    textFieldStartDay.getText(),
                    textFieldStartMonth.getText(),
                    textFieldStartYear.getText()
            );
            Date projectEndDate = CommonParse.parseDate(
                    textFieldDeadlineDay.getText(),
                    textFieldDeadlineMonth.getText(),
                    textFieldDeadlineYear.getText()
            );

            projectInformation.setStartDate(projectStartDate);
            projectInformation.setEndDate(projectEndDate);
            projectInformation.setStatus("Activo");
            projectInformation.setCompanyId(1);

            boolean isProjectSavedSuccessfully = projectManager.registerNewProject(projectInformation);

            if (isProjectSavedSuccessfully) {
                showSuccessAlert("Registro Exitoso", "El proyecto ha sido guardado correctamente.");
                closeCurrentWindow(actionEvent);
            }

        } catch (NumberFormatException capacityFormatMismatchException) {
            showErrorAlert("Error de formato", "El cupo de participantes debe ser un número entero válido.");
        } catch (IllegalArgumentException | DateTimeParseException dateValidationException) {
            showErrorAlert("Error de fecha", "Por favor, introduzca fechas válidas (DD/MM/AAAA).");
        } catch (ManagerException managerException) {
            showErrorAlert("Error al guardar", managerException.getMessage());
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

        int parsedDay = Integer.parseInt(day);
        int parsedMonth = Integer.parseInt(month);
        int parsedYear = Integer.parseInt(year);

        LocalDate convertedLocalDate = LocalDate.of(parsedYear, parsedMonth, parsedDay);
        return Date.valueOf(convertedLocalDate);
    }

    private void showSuccessAlert(String alertTitle, String alertMessage) {
        Alert userAlert = new Alert(Alert.AlertType.INFORMATION);
        userAlert.setTitle(alertTitle);
        userAlert.setHeaderText(null);
        userAlert.setContentText(alertMessage);
        userAlert.showAndWait();
    }

    private void showErrorAlert(String alertTitle, String alertMessage) {
        Alert userAlert = new Alert(Alert.AlertType.ERROR);
        userAlert.setTitle(alertTitle);
        userAlert.setHeaderText(null);
        userAlert.setContentText(alertMessage);
        userAlert.showAndWait();
    }
}