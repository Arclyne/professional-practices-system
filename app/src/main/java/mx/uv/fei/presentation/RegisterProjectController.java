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
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.domain.common.CommonParse;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.CommonControler;

import java.net.URL;
import java.sql.Date;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

@Component
public class RegisterProjectController implements Initializable {

    private final ProjectManager projectManager;

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
    private void handleActionSaveButton(ActionEvent actionEvent) {
        try {
            Project projectInformation = new Project();

            projectInformation.setProjectName(fieldProjectName.getText());
            projectInformation.setDescription(textAreaDescription.getText());
            projectInformation.setManager((String) comboBoxManager.getValue());

            int parsedProjectCapacity = Integer.parseInt(fieldCapacity.getText());
            projectInformation.setParticipantCapacity(parsedProjectCapacity);

            Date projectStartDate = CommonParse.parseDate(
                    textFieldStartDay.getText(),
                    textFieldStartMonth.getText(),
                    textFieldStartYear.getText());
            Date projectEndDate = CommonParse.parseDate(
                    textFieldDeadlineDay.getText(),
                    textFieldDeadlineMonth.getText(),
                    textFieldDeadlineYear.getText());

            projectInformation.setStartDate(projectStartDate);
            projectInformation.setEndDate(projectEndDate);
            projectInformation.setStatus("Activo");
            projectInformation.setCompanyId(1);

            boolean isProjectSavedSuccessfully = projectManager.registerNewProject(projectInformation);

            if (isProjectSavedSuccessfully) {
                CommonControler.showSuccessAlert("Registro Exitoso", "El proyecto ha sido guardado correctamente.");
            }

        } catch (NumberFormatException capacityFormatMismatchException) {
            CommonControler.showErrorAlert("Error de formato",
                    "El cupo de participantes debe ser un número entero válido.");
        } catch (IllegalArgumentException | DateTimeParseException dateValidationException) {
            CommonControler.showErrorAlert("Error de fecha", "Por favor, introduzca fechas válidas (DD/MM/AAAA).");
        } catch (ManagerException managerException) {
            CommonControler.showErrorAlert("Error al guardar", managerException.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }
}