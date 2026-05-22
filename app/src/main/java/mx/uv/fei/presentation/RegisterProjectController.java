package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.domain.statemachine.AppStore;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class RegisterProjectController implements Initializable {

    private final ProjectManager projectManager;
    private final OrganizationManager organizationManager;
    private final AppStore store;

    private final Map<String, Integer> organizationMap = new HashMap<>();
    private final Map<String, Integer> managerMap = new HashMap<>();

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

    @Inject
    public RegisterProjectController(ProjectManager projectManager, OrganizationManager organizationManager, AppStore store) {
        this.projectManager = projectManager;
        this.organizationManager = organizationManager;
        this.store = store;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {

        comboBoxManager.setDisable(true);
        loadOrganizations();

        comboBoxOrganization.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.toString().trim().isEmpty()) {
                int orgId = organizationMap.get((String) newValue);
                loadManagersByOrganization(orgId);
            } else {

                comboBoxManager.setDisable(true);
                comboBoxManager.getItems().clear();
            }
        });
    }

    private void loadOrganizations() {
        try {
            List<Organization> organizations = organizationManager.getAllOrganizations();
            ObservableList<String> organizationOptions = FXCollections.observableArrayList();

            for (Organization org : organizations) {
                if ("Activo".equalsIgnoreCase(org.getState())) { // Filtrar por activas
                    organizationOptions.add(org.getNameOrganization());
                    organizationMap.put(org.getNameOrganization(), org.getIdOrganization());
                }
            }
            comboBoxOrganization.setItems(organizationOptions);

        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de Carga", e.getMessage());
        }
    }

    private void loadManagersByOrganization(int organizationId) {
        try {
            List<Manager> managers = organizationManager.getManagersByOrganization(organizationId);
            ObservableList<String> managerNames = FXCollections.observableArrayList();

            managerMap.clear();

            for (Manager mgr : managers) {
                managerNames.add(mgr.getName());
                managerMap.put(mgr.getName(), mgr.getId());
            }

            comboBoxManager.setItems(managerNames);

            if (!managerNames.isEmpty()) {
                comboBoxManager.setDisable(false);
            } else {
                comboBoxManager.setDisable(true);
                comboBoxManager.setPromptText("Sin encargados registrados");
            }

        } catch (ManagerException e) {
            Controller.showErrorAlert("Error", e.getMessage());
            comboBoxManager.setDisable(true);
        }
    }

    @FXML
    private void handleActionSaveButton(ActionEvent actionEvent) {
        try {
            Project projectInformation = new Project();

            projectInformation.setProjectName(fieldProjectName.getText());
            projectInformation.setDescription(textAreaDescription.getText());

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

            String selectedOrg = (String) comboBoxOrganization.getValue();
            String selectedMgr = (String) comboBoxManager.getValue();

            if (selectedOrg == null || selectedMgr == null) {
                throw new ManagerException("Debe seleccionar una organización y un encargado asignado.");
            }

            projectInformation.setCompanyId(organizationMap.get(selectedOrg));
            projectInformation.setManagerId(managerMap.get(selectedMgr));

            boolean isProjectSavedSuccessfully = projectManager.registerNewProject(projectInformation);

            if (isProjectSavedSuccessfully) {
                Controller.showSuccessAlert("Registro Exitoso", "El proyecto ha sido guardado correctamente.");
                store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
            }

        } catch (NumberFormatException e) {
            Controller.showErrorAlert("Error de formato", "El cupo de participantes debe ser un número entero válido.");
        } catch (IllegalArgumentException | DateTimeParseException e) {
            Controller.showErrorAlert("Error de fecha", "Por favor, introduzca fechas válidas (DD/MM/AAAA).");
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al guardar", e.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}