package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.domain.common.Parse;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;

import java.net.URL;
import java.sql.Date;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

@Component
public class RegisterProjectController implements Initializable {

    private final ProjectManager projectManager;
    private final Store store;
    public Button saveButton;
    public Button cancelButton;

    @FXML
    private FormField fieldProjectName;
    @FXML
    private FormField fieldCapacity;
    @FXML
    private FormComboBox comboBoxOrganization;
    @FXML
    private FormComboBox comboBoxManager;

    @FXML
    private TextField textFieldStartDay;
    @FXML
    private TextField textFieldStartMonth;
    @FXML
    private TextField textFieldStartYear;

    @FXML
    private TextField textFieldDeadlineDay;
    @FXML
    private TextField textFieldDeadlineMonth;
    @FXML
    private TextField textFieldDeadlineYear;

    @FXML
    private TextArea textAreaDescription;

    @FXML
    private Button buttonSave;
    @FXML
    private Button buttonCancel;

    @Inject
    public RegisterProjectController(ProjectManager projectManager, Store store) {
        this.projectManager = projectManager;
        this.store = store;
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
    private void handleActionSaveButton(ActionEvent actionEvent) {
        try {
            Project projectInformation = new Project();

            projectInformation.setProjectName(fieldProjectName.getText());
            projectInformation.setDescription(textAreaDescription.getText());
            projectInformation.setManager((String) comboBoxManager.getValue());

            int parsedProjectCapacity = Integer.parseInt(fieldCapacity.getText());
            projectInformation.setParticipantCapacity(parsedProjectCapacity);

            Date projectStartDate = Parse.parseDate(
                    textFieldStartDay.getText(),
                    textFieldStartMonth.getText(),
                    textFieldStartYear.getText());
            Date projectEndDate = Parse.parseDate(
                    textFieldDeadlineDay.getText(),
                    textFieldDeadlineMonth.getText(),
                    textFieldDeadlineYear.getText());

            projectInformation.setStartDate(projectStartDate);
            projectInformation.setEndDate(projectEndDate);
            projectInformation.setStatus("Activo");
            projectInformation.setCompanyId(1);

            boolean isProjectSavedSuccessfully = projectManager.registerNewProject(projectInformation);

            if (isProjectSavedSuccessfully) {
                Controller.showSuccessAlert("Registro Exitoso", "El proyecto ha sido guardado correctamente.");
            }

        } catch (NumberFormatException capacityFormatMismatchException) {
            Controller.showErrorAlert("Error de formato",
                    "El cupo de participantes debe ser un número entero válido.");
        } catch (IllegalArgumentException | DateTimeParseException dateValidationException) {
            Controller.showErrorAlert("Error de fecha", "Por favor, introduzca fechas válidas (DD/MM/AAAA).");
        } catch (ManagerException managerException) {
            Controller.showErrorAlert("Error al guardar", managerException.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}