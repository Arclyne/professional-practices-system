package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PostulationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

@Component
public class ViewAssignedProjectController {

    @FXML private Label projectNameLabel;
    @FXML private Label projectStatusLabel;
    @FXML private Label projectPeriodLabel;
    @FXML private TextArea projectDescriptionTextArea;
    @FXML private Button returnButton;

    private final PostulationManager postulationManager;
    private final AppStore store;

    @Inject
    public ViewAssignedProjectController(PostulationManager postulationManager, AppStore store) {
        this.postulationManager = postulationManager;
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
                displayProject(assignedProject);
            } else {
                Controller.showAlert("Sin proyecto asignado",
                        "No se encontró un proyecto asignado para tu cuenta.", AlertType.INFORMATION);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    private void displayProject(Project project) {
        projectNameLabel.setText(project.getProjectName());
        projectStatusLabel.setText("Estado: " + project.getStatus());
        projectPeriodLabel.setText("Periodo: " + project.getStartDate() + " al " + project.getEndDate());
        projectDescriptionTextArea.setText(project.getDescription());
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}