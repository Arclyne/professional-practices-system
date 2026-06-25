package mx.uv.fei.presentation.practitioner;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PostulationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

@Component
public class PrioritizeProjectsController {

    private static final String ASSIGNED_PROJECT_VIEW = "/mx/uv/fei/presentation/viewAssignedProject.fxml";

    @FXML private VBox assignedStateContainer;
    @FXML private VBox prioritizationContainer;
    @FXML private Label assignedProjectNameLabel;
    @FXML private ListView<Project> availableProjectsListView;
    @FXML private ListView<Project> prioritizedProjectsListView;
    @FXML private Button assignProjectButton;
    @FXML private Button revokeProjectButton;
    @FXML private Button viewProjectInfoButton;
    @FXML private Button movePriorityUpButton;
    @FXML private Button movePriorityDownButton;
    @FXML private Button savePostulationButton;
    @FXML private Button cancelPostulationButton;

    private final PostulationManager postulationManager;
    private final AppStore store;
    private final ShellNavigator shellNavigator;

    private final ObservableList<Project> availableProjects = FXCollections.observableArrayList();
    private final ObservableList<Project> prioritizedProjects = FXCollections.observableArrayList();

    @Inject
    public PrioritizeProjectsController(PostulationManager postulationManager, AppStore store,
                                        ShellNavigator shellNavigator) {
        this.postulationManager = postulationManager;
        this.store = store;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        configureProjectListView(availableProjectsListView);
        configureProjectListView(prioritizedProjectsListView);
        availableProjectsListView.setItems(availableProjects);
        prioritizedProjectsListView.setItems(prioritizedProjects);
        if (!showAssignedProjectStateIfAny()) {
            loadAvailableProjects();
        }
    }

    private boolean showAssignedProjectStateIfAny() {
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            Project assignedProject = postulationManager.getAssignedProject(currentUser.getId());
            boolean hasAssignedProject = assignedProject != null && assignedProject.getProjectId() > 0;
            if (hasAssignedProject) {
                assignedProjectNameLabel.setText(assignedProject.getProjectName());
            }
            displayAssignedState(hasAssignedProject);
            return hasAssignedProject;
        } catch (ManagerException e) {
            Controller.showAlert("Error de conexión", e.getMessage(), AlertType.ERROR);
            displayAssignedState(false);
            return false;
        }
    }

    private void displayAssignedState(boolean hasAssignedProject) {
        assignedStateContainer.setVisible(hasAssignedProject);
        assignedStateContainer.setManaged(hasAssignedProject);
        prioritizationContainer.setVisible(!hasAssignedProject);
        prioritizationContainer.setManaged(!hasAssignedProject);
    }

    @FXML
    private void handleGoToAssignedProjectAction() {
        shellNavigator.showSubView(ASSIGNED_PROJECT_VIEW);
    }

    private void configureProjectListView(ListView<Project> projectListView) {
        projectListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Project project, boolean isEmpty) {
                super.updateItem(project, isEmpty);
                if (isEmpty || project == null || project.getProjectName() == null) {
                    setText(null);
                } else {
                    setText(project.getProjectName());
                }
            }
        });
    }

    private void loadAvailableProjects() {
        try {
            availableProjects.clear();
            prioritizedProjects.clear();
            List<Project> retrievedProjects = postulationManager.retrieveAllAvailableProjects();
            availableProjects.addAll(retrievedProjects);
        } catch (ManagerException e) {
            Controller.showAlert("Error de conexión", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleAssignProjectAction() {
        Project selectedProject = availableProjectsListView.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            availableProjects.remove(selectedProject);
            prioritizedProjects.add(selectedProject);
        } else {
            Controller.showAlert("Selección requerida",
                    "Por favor, seleccione un proyecto de la lista de disponibles.", AlertType.WARNING);
        }
    }

    @FXML
    private void handleViewProjectInfoAction() {
        Project selectedProject = resolveSelectedProject();
        if (selectedProject == null) {
            Controller.showAlert("Selección requerida",
                    "Selecciona un proyecto para ver su información.", AlertType.WARNING);
            return;
        }
        Controller.showAlert("Información del proyecto", buildProjectDetail(selectedProject), AlertType.INFORMATION);
    }

    private Project resolveSelectedProject() {
        Project selectedProject = availableProjectsListView.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            selectedProject = prioritizedProjectsListView.getSelectionModel().getSelectedItem();
        }
        return selectedProject;
    }

    private String buildProjectDetail(Project project) {
        return project.getProjectName()
                + "\n\nEstado: " + project.getStatus()
                + "\nCupos: " + project.getParticipantCapacity()
                + "\nPeriodo: " + project.getStartDate() + " al " + project.getEndDate()
                + "\n\n" + project.getDescription();
    }

    @FXML
    private void handleRevokeProjectAction() {
        Project selectedProject = prioritizedProjectsListView.getSelectionModel().getSelectedItem();
        if (selectedProject != null) {
            prioritizedProjects.remove(selectedProject);
            availableProjects.add(selectedProject);
        } else {
            Controller.showAlert("Selección requerida",
                    "Por favor, seleccione un proyecto de su lista de prioridades para devolverlo.", AlertType.WARNING);
        }
    }

    @FXML
    private void handleMovePriorityUpAction() {
        int selectedIndex = prioritizedProjectsListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex > 0) {
            swapPriorities(selectedIndex, selectedIndex - 1);
        }
    }

    @FXML
    private void handleMovePriorityDownAction() {
        int selectedIndex = prioritizedProjectsListView.getSelectionModel().getSelectedIndex();
        int lastIndex = prioritizedProjects.size() - 1;
        if (selectedIndex >= 0 && selectedIndex < lastIndex) {
            swapPriorities(selectedIndex, selectedIndex + 1);
        }
    }

    private void swapPriorities(int firstIndex, int secondIndex) {
        Project firstProject = prioritizedProjects.get(firstIndex);
        Project secondProject = prioritizedProjects.get(secondIndex);
        prioritizedProjects.set(secondIndex, firstProject);
        prioritizedProjects.set(firstIndex, secondProject);
        prioritizedProjectsListView.getSelectionModel().select(secondIndex);
    }

    @FXML
    private void handleSavePostulationAction() {
        if (!availableProjects.isEmpty()) {
            Controller.showAlert("Postulación Incompleta",
                    "Debe asignar una prioridad a todos los proyectos disponibles antes de guardar.",
                    AlertType.WARNING);
        } else {
            savePractitionerPriorities();
        }
    }

    private void savePractitionerPriorities() {
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            List<Project> priorities = new ArrayList<>(prioritizedProjects);
            postulationManager.registerPractitionerPriorities(currentUser.getId(), priorities);
            Controller.showAlert("Postulación Exitosa",
                    "Sus prioridades han sido registradas en el sistema correctamente.", AlertType.INFORMATION);
            loadAvailableProjects();
        } catch (ManagerException e) {
            Controller.showAlert("Error al guardar", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelPostulationAction() {
        loadAvailableProjects();
    }
}