package mx.uv.fei.presentation.coordinator;

import java.net.URL;
import java.sql.Date;
import java.time.format.DateTimeParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.DateParser;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.people.ManagerManager;
import mx.uv.fei.domain.manager.people.OrganizationManager;
import mx.uv.fei.domain.manager.academic.ProjectManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.shell.ShellNavigator;

@Component
public class RegisterProjectController implements Initializable {

    private static final String STATUS_ACTIVE = "Active";
    private static final String ERROR_TITLE = "Error";
    private static final String LOAD_ERROR_TITLE = "Error de Carga";
    private static final String SUCCESS_TITLE = "Operación Exitosa";
    private static final String SUCCESS_MESSAGE_REGISTER = "El proyecto ha sido guardado correctamente.";
    private static final String SUCCESS_MESSAGE_UPDATE = "El proyecto se ha actualizado correctamente.";
    private static final String FORMAT_ERROR_TITLE = "Error de formato";
    private static final String FORMAT_ERROR_MESSAGE = "El cupo de participantes debe ser un número entero válido.";
    private static final String DATE_ERROR_TITLE = "Error de fecha";
    private static final String DATE_ERROR_MESSAGE = "Por favor, introduzca fechas válidas (DD/MM/AAAA).";
    private static final String SAVE_ERROR_TITLE = "Error al guardar";
    private static final String VALIDATION_ERROR_MESSAGE = "Debe seleccionar una organización y un encargado asignado.";
    private static final String NO_MANAGERS_TEXT = "Sin encargados registrados";

    private final ProjectManager projectManager;
    private final OrganizationManager organizationManager;
    private final ManagerManager managerManager;
    private final ShellNavigator shellNavigator;

    private final Map<String, Integer> organizationMap = new HashMap<>();
    private final Map<String, Integer> managerMap = new HashMap<>();
    private Project projectBeingEdited;

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
    @FXML private Button saveButton;

    @Inject
    public RegisterProjectController(ProjectManager projectManager,
                                     OrganizationManager organizationManager,
                                     ManagerManager managerManager,
                                     ShellNavigator shellNavigator) {
        this.projectManager = projectManager;
        this.organizationManager = organizationManager;
        this.managerManager = managerManager;
        this.shellNavigator = shellNavigator;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
        comboBoxManager.setDisable(true);
        loadOrganizations();

        comboBoxOrganization.valueProperty().addListener((_, _, newValue) -> {
            if (newValue != null && !newValue.toString().trim().isEmpty()) {
                int orgId = organizationMap.get((String) newValue);
                loadManagersByOrganization(orgId);
            } else {
                comboBoxManager.setDisable(true);
                comboBoxManager.getItems().clear();
            }
        });

        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof Project) {
            projectBeingEdited = (Project) pendingEntity;
            populateForm(projectBeingEdited);
            saveButton.setText("Guardar cambios");
        } else {
            projectBeingEdited = null;
            saveButton.setText("Guardar");
        }
    }

    private void populateForm(Project project) {
        fieldProjectName.setText(project.getProjectName());
        fieldCapacity.setText(String.valueOf(project.getParticipantCapacity()));
        textAreaDescription.setText(project.getDescription());

        if (project.getStartDate() != null) {
            LocalDate start = project.getStartDate().toLocalDate();
            textFieldStartDay.setText(String.format("%02d", start.getDayOfMonth()));
            textFieldStartMonth.setText(String.format("%02d", start.getMonthValue()));
            textFieldStartYear.setText(String.valueOf(start.getYear()));
        }

        if (project.getEndDate() != null) {
            LocalDate end = project.getEndDate().toLocalDate();
            textFieldDeadlineDay.setText(String.format("%02d", end.getDayOfMonth()));
            textFieldDeadlineMonth.setText(String.format("%02d", end.getMonthValue()));
            textFieldDeadlineYear.setText(String.valueOf(end.getYear()));
        }

        String orgName = organizationMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(project.getCompanyId()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (orgName != null) {
            comboBoxOrganization.setValue(orgName);
            String mgrName = managerMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(project.getManagerId()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(null);
            if (mgrName != null) {
                comboBoxManager.setValue(mgrName);
            }
        }
    }

    private void loadOrganizations() {
        try {
            List<Organization> organizations = organizationManager.getAllOrganizations();
            ObservableList<String> organizationOptions = FXCollections.observableArrayList();

            for (Organization org : organizations) {
                if (STATUS_ACTIVE.equalsIgnoreCase(org.getState())) {
                    organizationOptions.add(org.getNameOrganization());
                    organizationMap.put(org.getNameOrganization(), org.getIdOrganization());
                }
            }
            comboBoxOrganization.setItems(organizationOptions);

        } catch (ManagerException e) {
            Controller.showErrorAlert(LOAD_ERROR_TITLE, e.getMessage());
        }
    }

    private void loadManagersByOrganization(int organizationId) {
        try {
            List<Manager> managers = managerManager.getManagersByOrganization(organizationId);
            ObservableList<String> managerNames = FXCollections.observableArrayList();
            managerMap.clear();

            for (Manager mgr : managers) {
                if (mgr.getStatus() != null && "Active".equalsIgnoreCase(mgr.getStatus().getDatabaseValue())) {
                    managerNames.add(mgr.getName());
                    managerMap.put(mgr.getName(), mgr.getId());
                }
            }

            comboBoxManager.setItems(managerNames);
            comboBoxManager.setDisable(managerNames.isEmpty());

            if (managerNames.isEmpty()) {
                comboBoxManager.setPromptText(NO_MANAGERS_TEXT);
            } else {
                comboBoxManager.setPromptText("");
            }

        } catch (ManagerException e) {
            Controller.showErrorAlert(ERROR_TITLE, e.getMessage());
            comboBoxManager.setDisable(true);
        }
    }

    @FXML
    private void handleActionSaveButton() {
        try {
            Project projectInformation = new Project();
            projectInformation.setProjectName(fieldProjectName.getText());
            projectInformation.setDescription(textAreaDescription.getText());
            projectInformation.setParticipantCapacity(Integer.parseInt(fieldCapacity.getText()));
            projectInformation.setStartDate(DateParser.parseDate(textFieldStartDay.getText(), textFieldStartMonth.getText(), textFieldStartYear.getText()));
            projectInformation.setEndDate(DateParser.parseDate(textFieldDeadlineDay.getText(), textFieldDeadlineMonth.getText(), textFieldDeadlineYear.getText()));
            projectInformation.setStatus(STATUS_ACTIVE);

            String selectedOrg = (String) comboBoxOrganization.getValue();
            String selectedMgr = (String) comboBoxManager.getValue();

            if (selectedOrg == null || selectedMgr == null) {
                throw new ManagerException(VALIDATION_ERROR_MESSAGE);
            }

            projectInformation.setCompanyId(organizationMap.get(selectedOrg));
            projectInformation.setManagerId(managerMap.get(selectedMgr));

            if (projectBeingEdited != null) {
                projectManager.updateProject(projectInformation, projectBeingEdited.getProjectId());
                Controller.showSuccessAlert(SUCCESS_TITLE, SUCCESS_MESSAGE_UPDATE);
            } else {
                projectManager.registerNewProject(projectInformation);
                Controller.showSuccessAlert(SUCCESS_TITLE, SUCCESS_MESSAGE_REGISTER);
            }

            shellNavigator.returnToList();

        } catch (NumberFormatException _) {
            Controller.showErrorAlert(FORMAT_ERROR_TITLE, FORMAT_ERROR_MESSAGE);
        } catch (IllegalArgumentException | DateTimeParseException _) {
            Controller.showErrorAlert(DATE_ERROR_TITLE, DATE_ERROR_MESSAGE);
        } catch (ManagerException e) {
            Controller.showErrorAlert(SAVE_ERROR_TITLE, e.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton() {
        shellNavigator.returnToList();
    }
}