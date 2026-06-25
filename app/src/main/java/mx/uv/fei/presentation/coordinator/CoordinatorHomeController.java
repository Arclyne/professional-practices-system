package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.people.OrganizationManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.domain.manager.people.ProfessorManager;
import mx.uv.fei.domain.manager.academic.ProjectManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.List;

@Component
public class CoordinatorHomeController {

    @FXML private Label practitionersCountLabel;
    @FXML private Label projectsCountLabel;
    @FXML private Label organizationsCountLabel;
    @FXML private Label professorsCountLabel;
    @FXML private Label activePractitionersLabel;
    @FXML private Label inactivePractitionersLabel;
    @FXML private Label pendingPractitionersLabel;

    private final PractitionerManager practitionerManager;
    private final ProjectManager projectManager;
    private final OrganizationManager organizationManager;
    private final ProfessorManager professorManager;

    @Inject
    public CoordinatorHomeController(PractitionerManager practitionerManager, ProjectManager projectManager,
            OrganizationManager organizationManager, ProfessorManager professorManager) {
        this.practitionerManager = practitionerManager;
        this.projectManager = projectManager;
        this.organizationManager = organizationManager;
        this.professorManager = professorManager;
    }

    @FXML
    public void initialize() {
        try {
            List<Practitioner> practitioners = practitionerManager.getAllPractitioners();
            populateSummaryCards(practitioners);
            populateStatusDistribution(practitioners);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void populateSummaryCards(List<Practitioner> practitioners) throws ManagerException {
        practitionersCountLabel.setText(String.valueOf(practitioners.size()));
        projectsCountLabel.setText(String.valueOf(projectManager.getAllProjects().size()));
        organizationsCountLabel.setText(String.valueOf(organizationManager.getAllOrganizations().size()));
        professorsCountLabel.setText(String.valueOf(professorManager.getAllProfessors().size()));
    }

    private void populateStatusDistribution(List<Practitioner> practitioners) {
        activePractitionersLabel.setText(String.valueOf(countByStatus(practitioners, UserStatus.ACTIVE)));
        inactivePractitionersLabel.setText(String.valueOf(countByStatus(practitioners, UserStatus.INACTIVE)));
        pendingPractitionersLabel.setText(String.valueOf(countByStatus(practitioners, UserStatus.PENDING)));
    }

    private long countByStatus(List<Practitioner> practitioners, UserStatus status) {
        return practitioners.stream()
                .filter(practitioner -> practitioner.getStatus() == status)
                .count();
    }
}
