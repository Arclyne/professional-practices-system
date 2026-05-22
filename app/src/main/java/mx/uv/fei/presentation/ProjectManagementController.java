package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProjectManagementController {

    @FXML private ListView<String> projectsListView;

    private final ProjectManager projectManager;
    private final AppStore applicationNavigationStore;

    private final Map<String, Integer> itemToIdentifierMap = new HashMap<>();
    private final Map<String, BooleanProperty> itemSelectionStateMap = new HashMap<>();

    @Inject
    public ProjectManagementController(ProjectManager projectManager, AppStore applicationNavigationStore) {
        this.projectManager = projectManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    public void initialize() {
        projectsListView.setCellFactory(CheckBoxListCell.forListView(itemString -> itemSelectionStateMap.get(itemString)));
        projectsListView.setOnMouseClicked(event -> {
            String selectedItem = projectsListView.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                BooleanProperty checkboxState = itemSelectionStateMap.get(selectedItem);
                if (checkboxState != null) {
                    checkboxState.set(!checkboxState.get());
                    projectsListView.getSelectionModel().clearSelection();
                }
            }
        });

        loadActiveProjects();
    }

    private void loadActiveProjects() {
        itemToIdentifierMap.clear();
        itemSelectionStateMap.clear();
        ObservableList<String> displayItemsList = FXCollections.observableArrayList();

        try {
            List<Project> registeredProjectsList = projectManager.getAllProjects();
            for (Project currentProject : registeredProjectsList) {
                if (currentProject.getStatus() != null && !"Inactive".equalsIgnoreCase(currentProject.getStatus())) {

                    String formattedDisplayString = currentProject.getProjectName() + " (Cupo: " + currentProject.getParticipantCapacity() + ") - " + currentProject.getStatus();
                    itemToIdentifierMap.put(formattedDisplayString, currentProject.getProjectId());
                    itemSelectionStateMap.put(formattedDisplayString, new SimpleBooleanProperty(false));
                    displayItemsList.add(formattedDisplayString);
                }
            }
        } catch (ManagerException dataRetrievalException) {
            Controller.showAlert("Error de carga", dataRetrievalException.getMessage(), AlertType.ERROR);
        }

        projectsListView.setItems(displayItemsList);
    }

    @FXML
    private void handleInactivateSelectedAction(ActionEvent userActionEvent) {
        List<Integer> identifiersToInactivateList = new ArrayList<>();

        for (Map.Entry<String, BooleanProperty> currentMapEntry : itemSelectionStateMap.entrySet()) {
            if (currentMapEntry.getValue().get()) {
                identifiersToInactivateList.add(itemToIdentifierMap.get(currentMapEntry.getKey()));
            }
        }

        if (identifiersToInactivateList.isEmpty()) {
            Controller.showAlert("Sin selección", "Debe seleccionar al menos un proyecto para inactivar.", AlertType.WARNING);
            return;
        }

        try {
            projectManager.inactivateMultipleProjects(identifiersToInactivateList);
            Controller.showAlert("Proceso Exitoso", "Los proyectos seleccionados han sido inactivados.", AlertType.INFORMATION);
            loadActiveProjects();
        } catch (ManagerException executionException) {
            Controller.showAlert("Error en la Operación", executionException.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleRegisterNewProjectAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PROJECT));
    }

    @FXML
    private void handleReturnAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}