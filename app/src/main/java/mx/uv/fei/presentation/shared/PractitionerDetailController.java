package mx.uv.fei.presentation.shared;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PostulationManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.people.ManagerManager;
import mx.uv.fei.domain.manager.people.OrganizationManager;
import mx.uv.fei.domain.manager.reporting.ProgressReportManager;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PractitionerDetailController {

    private static final String NO_PRACTITIONER_MESSAGE =
            "No se pudo abrir el detalle: practicante no disponible.";
    private static final String LOAD_ERROR_MESSAGE =
            "Ocurrió un error al cargar la información del practicante.";
    private static final String NO_GROUP_LABEL = "Sin grupo";
    private static final String NO_VALUE_LABEL = "Sin información";
    private static final String HOURS_FORMAT = "%.1f h";
    private static final String GRADE_FORMAT = "%.2f / 10.0";

    @FXML private Label loadErrorLabel;
    @FXML private ScrollPane contentScrollPane;

    @FXML private Label hoursCompletedLabel;
    @FXML private Label hoursRequiredLabel;
    @FXML private Label hoursRemainingLabel;

    @FXML private Label nameLabel;
    @FXML private Label enrollmentLabel;
    @FXML private Label userNameLabel;
    @FXML private Label emailLabel;
    @FXML private Label groupLabel;
    @FXML private Label gradeLabel;

    @FXML private Node projectContainer;
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
    @FXML private Node noProjectContainer;

    private final PostulationManager postulationManager;
    private final OrganizationManager organizationManager;
    private final ManagerManager managerManager;
    private final ProgressReportManager progressReportManager;
    private final PracticeGroupManager practiceGroupManager;
    private final ShellNavigator shellNavigator;

    @Inject
    public PractitionerDetailController(
            PostulationManager postulationManager,
            OrganizationManager organizationManager,
            ManagerManager managerManager,
            ProgressReportManager progressReportManager,
            PracticeGroupManager practiceGroupManager,
            ShellNavigator shellNavigator
    ) {
        this.postulationManager = postulationManager;
        this.organizationManager = organizationManager;
        this.managerManager = managerManager;
        this.progressReportManager = progressReportManager;
        this.practiceGroupManager = practiceGroupManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof Practitioner practitioner) {
            loadDetail(practitioner);
        } else {
            showLoadError(NO_PRACTITIONER_MESSAGE);
        }
    }

    private void loadDetail(Practitioner practitioner) {
        try {
            displayHours(practitioner.getId());
            displayPractitioner(practitioner);
            displayAssignedProject(practitioner.getId());
        } catch (ManagerException e) {
            showLoadError(LOAD_ERROR_MESSAGE);
        }
    }

    private void displayHours(int practitionerId) throws ManagerException {
        double completedHours = progressReportManager.getAccumulatedHours(practitionerId);
        int requiredHours = ProgressReportType.FINAL.getRequiredHours();
        double remainingHours = Math.max(0, requiredHours - completedHours);

        hoursCompletedLabel.setText(String.format(HOURS_FORMAT, completedHours));
        hoursRequiredLabel.setText(String.format(HOURS_FORMAT, (double) requiredHours));
        hoursRemainingLabel.setText(String.format(HOURS_FORMAT, remainingHours));
    }

    private void displayPractitioner(Practitioner practitioner) throws ManagerException {
        nameLabel.setText(practitioner.getName() + " " + practitioner.getLastName());
        enrollmentLabel.setText("Matrícula: " + practitioner.getEnrollment());
        userNameLabel.setText("Usuario: " + practitioner.getUserName());
        emailLabel.setText("Correo: " + practitioner.getEmail());
        groupLabel.setText("Grupo: " + sectionLabelOf(practitioner));
        gradeLabel.setText("Calificación: " + String.format(GRADE_FORMAT, practitioner.getGrade()));
    }

    private String sectionLabelOf(Practitioner practitioner) throws ManagerException {
        String section = NO_GROUP_LABEL;
        if (practitioner.getGroupId() != null) {
            PracticeGroup group = practiceGroupManager.getPracticeGroupById(practitioner.getGroupId());
            if (group != null && group.getSection() != null) {
                section = group.getSection();
            }
        }

        return section;
    }

    private void displayAssignedProject(int practitionerId) throws ManagerException {
        Project project = postulationManager.getAssignedProject(practitionerId);
        boolean hasProject = project.getProjectId() > 0;
        setNodeVisible(projectContainer, hasProject);
        setNodeVisible(noProjectContainer, !hasProject);

        if (hasProject) {
            displayProject(project);
            displayOrganization(project.getCompanyId());
            displayManager(project.getManagerId());
        }
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

    private void showLoadError(String message) {
        loadErrorLabel.setText(message);
        setNodeVisible(loadErrorLabel, true);
        setNodeVisible(contentScrollPane, false);
    }

    @FXML
    private void handleReturnAction() {
        shellNavigator.returnToList();
    }

    private void setNodeVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
