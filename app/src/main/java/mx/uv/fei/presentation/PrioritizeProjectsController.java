package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ProjectPrioritizationManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.util.ArrayList;
import java.util.List;

@Component
public class PrioritizeProjectsController {

    @FXML
    private ListView<Project> availableProjectsListView;
    @FXML
    private ListView<Project> prioritizedProjectsListView;
    @FXML
    private Button assignProjectButton;
    @FXML
    private Button revokeProjectButton;
    @FXML
    private Button movePriorityUpButton;
    @FXML
    private Button movePriorityDownButton;
    @FXML
    private Button savePostulationButton;
    @FXML
    private Button cancelPostulationButton;

    private final ProjectPrioritizationManager projectPrioritizationManager;
    private final Store applicationNavigationStore;

    private final ObservableList<Project> availableProjectsObservableList = FXCollections.observableArrayList();
    private final ObservableList<Project> prioritizedProjectsObservableList = FXCollections.observableArrayList();

    @Inject
    public PrioritizeProjectsController(ProjectPrioritizationManager projectPrioritizationManager, Store applicationNavigationStore) {
        this.projectPrioritizationManager = projectPrioritizationManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    public void initialize() {
        configureListViewProjectDisplay(availableProjectsListView);
        configureListViewProjectDisplay(prioritizedProjectsListView);
        availableProjectsListView.setItems(availableProjectsObservableList);
        prioritizedProjectsListView.setItems(prioritizedProjectsObservableList);
        loadSystemAvailableProjects();
    }

    private void configureListViewProjectDisplay(ListView<Project> targetProjectListView) {
        targetProjectListView.setCellFactory(parameter -> new ListCell<>() {
            @Override
            protected void updateItem(Project currentProjectItem, boolean isItemEmpty) {
                super.updateItem(currentProjectItem, isItemEmpty);
                if (isItemEmpty || currentProjectItem == null || currentProjectItem.getProjectName() == null) {
                    setText(null);
                } else {
                    setText(currentProjectItem.getProjectName());
                }
            }
        });
    }

    private void loadSystemAvailableProjects() {
        try {
            availableProjectsObservableList.clear();
            prioritizedProjectsObservableList.clear();
            List<Project> retrievedAvailableProjectsList = projectPrioritizationManager.retrieveAllAvailableProjects();
            availableProjectsObservableList.addAll(retrievedAvailableProjectsList);
        } catch (ManagerException managerRetrievalException) {
            Controller.showAlert("Error de conexion", managerRetrievalException.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleAssignProjectAction(ActionEvent userActionEvent) {
        Project selectedProjectToAssign = availableProjectsListView.getSelectionModel().getSelectedItem();

        if (selectedProjectToAssign != null) {
            availableProjectsObservableList.remove(selectedProjectToAssign);
            prioritizedProjectsObservableList.add(selectedProjectToAssign);
        } else {
            Controller.showAlert("Seleccion requerida", "Por favor, seleccione un proyecto de la lista de disponibles.", AlertType.WARNING);
        }
    }

    @FXML
    private void handleRevokeProjectAction(ActionEvent userActionEvent) {
        Project selectedProjectToRevoke = prioritizedProjectsListView.getSelectionModel().getSelectedItem();

        if (selectedProjectToRevoke != null) {
            prioritizedProjectsObservableList.remove(selectedProjectToRevoke);
            availableProjectsObservableList.add(selectedProjectToRevoke);
        } else {
            Controller.showAlert("Seleccion requerida", "Por favor, seleccione un proyecto de su lista de prioridades para devolverlo.", AlertType.WARNING);
        }
    }

    @FXML
    private void handleMovePriorityUpAction(ActionEvent userActionEvent) {
        int currentlySelectedProjectIndex = prioritizedProjectsListView.getSelectionModel().getSelectedIndex();

        if (currentlySelectedProjectIndex > 0) {
            Project projectToMoveUpwards = prioritizedProjectsObservableList.get(currentlySelectedProjectIndex);
            Project projectToSwapPositionsWith = prioritizedProjectsObservableList.get(currentlySelectedProjectIndex - 1);
            prioritizedProjectsObservableList.set(currentlySelectedProjectIndex - 1, projectToMoveUpwards);
            prioritizedProjectsObservableList.set(currentlySelectedProjectIndex, projectToSwapPositionsWith);
            prioritizedProjectsListView.getSelectionModel().select(currentlySelectedProjectIndex - 1);
        }
    }

    @FXML
    private void handleMovePriorityDownAction(ActionEvent userActionEvent) {
        int currentlySelectedProjectIndex = prioritizedProjectsListView.getSelectionModel().getSelectedIndex();
        int maximumValidListIndex = prioritizedProjectsObservableList.size() - 1;

        if (currentlySelectedProjectIndex >= 0 && currentlySelectedProjectIndex < maximumValidListIndex) {
            Project projectToMoveDownwards = prioritizedProjectsObservableList.get(currentlySelectedProjectIndex);
            Project projectToSwapPositionsWith = prioritizedProjectsObservableList.get(currentlySelectedProjectIndex + 1);
            prioritizedProjectsObservableList.set(currentlySelectedProjectIndex + 1, projectToMoveDownwards);
            prioritizedProjectsObservableList.set(currentlySelectedProjectIndex, projectToSwapPositionsWith);
            prioritizedProjectsListView.getSelectionModel().select(currentlySelectedProjectIndex + 1);
        }
    }

    @FXML
    private void handleSavePostulationAction(ActionEvent userActionEvent) {
        if (!availableProjectsObservableList.isEmpty()) {
            Controller.showAlert("Postulacion Incompleta", "Debe asignar una prioridad a todos los proyectos disponibles antes de guardar.", AlertType.WARNING);
        } else {
            try {
                User currentAuthenticatedPractitioner = applicationNavigationStore.getState().sessionState().currentUserInSession();
                List<Project> finalizedPriorityProjectList = new ArrayList<>(prioritizedProjectsObservableList);
                projectPrioritizationManager.registerPractitionerPriorities(currentAuthenticatedPractitioner.getId(), finalizedPriorityProjectList);
                Controller.showAlert("Postulacion Exitosa", "Sus prioridades han sido registradas en el sistema correctamente.", AlertType.INFORMATION);
                applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
            } catch (ManagerException priorityRegistrationException) {
                Controller.showAlert("Error al guardar", priorityRegistrationException.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleCancelPostulationAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}