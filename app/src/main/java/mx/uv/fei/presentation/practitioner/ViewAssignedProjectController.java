package mx.uv.fei.presentation.practitioner;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PostulationManager;
import mx.uv.fei.domain.manager.people.ManagerManager;
import mx.uv.fei.domain.manager.people.OrganizationManager;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

@Component
public class ViewAssignedProjectController {

    private static final String NO_VALUE_LABEL = "Sin información";

    @FXML private Label projectNameLabel;
    @FXML private Label projectStatusLabel;
    @FXML private Label projectCapacityLabel;
    @FXML private Label projectPeriodLabel;
    @FXML private TextArea projectDescriptionTextArea;

    @FXML private Label organizationNameLabel;
    @FXML private Label organizationCityLabel;
    @FXML private Label organizationAddressLabel;
    @FXML private Label organizationBusinessLabel;
    @FXML private Label organizationContactLabel;

    @FXML private Label managerNameLabel;
    @FXML private Label managerContactLabel;

    private final PostulationManager postulationManager;
    private final OrganizationManager organizationManager;
    private final ManagerManager managerManager;
    private final AppStore store;

    @Inject
    public ViewAssignedProjectController(PostulationManager postulationManager,
                                         OrganizationManager organizationManager, ManagerManager managerManager,
                                         AppStore store) {
        this.postulationManager = postulationManager;
        this.organizationManager = organizationManager;
        this.managerManager = managerManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        loadAssignedProject();
    }

    private void loadAssignedProject() {
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            Project assignedProject = postulationManager.getAssignedProject(currentUser.getId());
            if (assignedProject.getProjectId() > 0) {
                displayAssignedProject(assignedProject);
            } else {
                Controller.showAlert("Sin proyecto asignado",
                        "No se encontró un proyecto asignado para tu cuenta.", AlertType.INFORMATION);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private void displayAssignedProject(Project project) throws ManagerException {
        displayProject(project);
        displayOrganization(project.getCompanyId());
        displayManager(project.getManagerId());
    }

    private void displayProject(Project project) {
        projectNameLabel.setText(project.getProjectName());
        projectStatusLabel.setText("Estado: " + project.getStatus());
        projectCapacityLabel.setText("Cupos: " + project.getParticipantCapacity());
        projectPeriodLabel.setText("Periodo: " + project.getStartDate() + " al " + project.getEndDate());
        projectDescriptionTextArea.setText(project.getDescription());
    }

    private void displayOrganization(int organizationId) throws ManagerException {
        Organization organization = organizationManager.getOrganizationById(organizationId);
        organizationNameLabel.setText(textOrDefault(organization.getNameOrganization()));
        organizationCityLabel.setText("Ciudad: " + textOrDefault(organization.getCity()));
        organizationAddressLabel.setText("Dirección: " + textOrDefault(organization.getAdress()));
        organizationBusinessLabel.setText("Giro: " + textOrDefault(organization.getBusiness()));
        organizationContactLabel.setText("Contacto: " + textOrDefault(organization.getMail())
                + " · " + textOrDefault(organization.getCellphone()));
    }

    private void displayManager(int managerId) throws ManagerException {
        Manager manager = managerManager.getManagerById(managerId);
        managerNameLabel.setText(textOrDefault(manager.getName()));
        managerContactLabel.setText("Contacto: " + textOrDefault(manager.getEmail())
                + " · " + textOrDefault(manager.getPhone()));
    }

    private String textOrDefault(String value) {
        return value != null && !value.isBlank() ? value : NO_VALUE_LABEL;
    }
}
