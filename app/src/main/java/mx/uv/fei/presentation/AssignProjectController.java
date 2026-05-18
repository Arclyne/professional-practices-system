package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.ProjectPostulation;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ProjectAssignmentManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

import java.util.List;

@Component
public class AssignProjectController {

    @FXML
    private Label practitionerInformationLabel;
    @FXML
    private ListView<ProjectPostulation> practitionerPostulationsListView;
    @FXML
    private Button confirmAssignmentButton;
    @FXML
    private Button returnToDashboardButton;

    private final ProjectAssignmentManager projectAssignmentManager;
    private final Store applicationNavigationStore;
    private final ObservableList<ProjectPostulation> postulationsObservableList = FXCollections.observableArrayList();

    private int targetPractitionerIdentifier;

    @Inject
    public AssignProjectController(ProjectAssignmentManager projectAssignmentManager, Store applicationNavigationStore) {
        this.projectAssignmentManager = projectAssignmentManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    public void initialize() {
        try {
            postulationsObservableList.clear();
            RootState currentSystemState = applicationNavigationStore.getState();
            String retrievedEntityIdentifier = currentSystemState.navigationState().targetEntityId();

            if (retrievedEntityIdentifier != null && !retrievedEntityIdentifier.isEmpty()) {
                this.targetPractitionerIdentifier = Integer.parseInt(retrievedEntityIdentifier);
                practitionerInformationLabel.setText("Practicante Seleccionado (ID): " + targetPractitionerIdentifier);

                configurePostulationListViewDisplay();
                practitionerPostulationsListView.setItems(postulationsObservableList);
                loadPractitionerPostulations();
            } else {
                Controller.showAlert("Información faltante", "No se pudo recuperar la información del practicante seleccionado.", AlertType.WARNING);
            }
        } catch (Exception initializationException) {
            Controller.showAlert("Error de carga", "Ocurrió un problema al inicializar la pantalla de asignación.", AlertType.ERROR);
        }
    }

    private void configurePostulationListViewDisplay() {
        practitionerPostulationsListView.setCellFactory(parameter -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectPostulation postulationItem, boolean isItemEmpty) {
                super.updateItem(postulationItem, isItemEmpty);
                if (isItemEmpty || postulationItem == null) {
                    setText(null);
                } else {
                    String formattedDisplayString = "Prioridad " + postulationItem.getPriorityLevel() + " - " + postulationItem.getProjectName() + " (" + postulationItem.getPostulationStatus() + ")";
                    setText(formattedDisplayString);
                }
            }
        });
    }

    private void loadPractitionerPostulations() {
        try {
            postulationsObservableList.clear();
            List<ProjectPostulation> retrievedPostulationsList = projectAssignmentManager.retrievePractitionerPostulations(targetPractitionerIdentifier);
            postulationsObservableList.addAll(retrievedPostulationsList);
        } catch (ManagerException managerRetrievalException) {
            Controller.showAlert("Error de conexión", managerRetrievalException.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleConfirmAssignmentAction(ActionEvent userActionEvent) {
        ProjectPostulation selectedPostulationToAssign = practitionerPostulationsListView.getSelectionModel().getSelectedItem();

        if (selectedPostulationToAssign != null) {
            try {
                projectAssignmentManager.assignProjectToPractitioner(targetPractitionerIdentifier, selectedPostulationToAssign.getProjectIdentifier());
                Controller.showAlert("Asignación Exitosa", "El proyecto ha sido asignado al practicante correctamente.", AlertType.INFORMATION);
                applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_PRACTITIONER_MENU));
            } catch (ManagerException assignmentException) {
                Controller.showAlert("Error en la asignación", assignmentException.getMessage(), AlertType.ERROR);
            }
        } else {
            Controller.showAlert("Selección requerida", "Por favor, seleccione un proyecto de la lista para asignarlo.", AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnToDashboardAction(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_PRACTITIONER_MENU));
    }
}