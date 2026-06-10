package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProjectManagementController {

    private static final String STATUS_INACTIVE = "Inactive";
    private static final String PROJECT_DISPLAY_FORMAT = "%s (Cupo: %d) - %s";

    @FXML private ListView<String> projectsListView;

    private final ProjectManager projectManager;
    private final AppStore store;

    private final Map<String, Integer> displayToProjectId = new HashMap<>();
    private final Map<String, BooleanProperty> displaySelectionState = new HashMap<>();

    @Inject
    public ProjectManagementController(ProjectManager projectManager, AppStore store) {
        this.projectManager = projectManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        Controller.setupCheckBoxListView(projectsListView, displaySelectionState);
        loadActiveProjects();
    }

    private void loadActiveProjects() {
        displayToProjectId.clear();
        displaySelectionState.clear();
        ObservableList<String> displayItems = FXCollections.observableArrayList();

        try {
            List<Project> projects = projectManager.getAllProjects();
            for (Project project : projects) {
                if (isProjectActive(project)) {
                    addProjectToDisplay(project, displayItems);
                }
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de carga", e.getMessage(), AlertType.ERROR);
        }

        projectsListView.setItems(displayItems);
    }

    private boolean isProjectActive(Project project) {
        return project.getStatus() != null && !STATUS_INACTIVE.equalsIgnoreCase(project.getStatus());
    }

    private void addProjectToDisplay(Project project, ObservableList<String> displayItems) {
        String displayText = String.format(PROJECT_DISPLAY_FORMAT,
                project.getProjectName(), project.getParticipantCapacity(), project.getStatus());
        displayToProjectId.put(displayText, project.getProjectId());
        displaySelectionState.put(displayText, new SimpleBooleanProperty(false));
        displayItems.add(displayText);
    }

    @FXML
    private void handleInactivateSelectedAction() {
        List<Integer> selectedProjectIds = collectSelectedProjectIds();

        if (selectedProjectIds.isEmpty()) {
            Controller.showAlert("Sin selección",
                    "Debe seleccionar al menos un proyecto para inactivar.", AlertType.WARNING);
            return;
        }

        try {
            projectManager.inactivateMultipleProjects(selectedProjectIds);
            Controller.showAlert("Proceso Exitoso",
                    "Los proyectos seleccionados han sido inactivados.", AlertType.INFORMATION);
            loadActiveProjects();
        } catch (ManagerException e) {
            Controller.showAlert("Error en la Operación", e.getMessage(), AlertType.ERROR);
        }
    }

    private List<Integer> collectSelectedProjectIds() {
        List<Integer> selectedProjectIds = new ArrayList<>();
        for (Map.Entry<String, BooleanProperty> selectionEntry : displaySelectionState.entrySet()) {
            if (selectionEntry.getValue().get()) {
                selectedProjectIds.add(displayToProjectId.get(selectionEntry.getKey()));
            }
        }
        return selectedProjectIds;
    }

    @FXML
    private void handleRegisterNewProjectAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PROJECT));
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}