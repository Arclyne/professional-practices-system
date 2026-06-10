package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.ProjectPostulation;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PostulationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.RootState;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.List;

@Component
public class AssignProjectController {

    @FXML private Label practitionerInformationLabel;
    @FXML private ListView<ProjectPostulation> practitionerPostulationsListView;
    @FXML private Button confirmAssignmentButton;
    @FXML private Button returnToDashboardButton;

    private final PostulationManager postulationManager;
    private final AppStore store;
    private final ObservableList<ProjectPostulation> postulations = FXCollections.observableArrayList();

    private int targetPractitionerId;

    @Inject
    public AssignProjectController(PostulationManager postulationManager, AppStore store) {
        this.postulationManager = postulationManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        try {
            RootState currentState = store.getState();
            String entityId = currentState.navigationState().targetEntityId();

            if (entityId != null && !entityId.isEmpty()) {
                targetPractitionerId = Integer.parseInt(entityId);
                practitionerInformationLabel.setText("Practicante Seleccionado (ID): " + targetPractitionerId);
                configurePostulationListView();
                practitionerPostulationsListView.setItems(postulations);
                loadPractitionerPostulations();
            } else {
                Controller.showAlert("Información faltante",
                        "No se pudo recuperar la información del practicante seleccionado.", AlertType.WARNING);
            }
        } catch (Exception e) {
            Controller.showAlert("Error de carga",
                    "Ocurrió un problema al inicializar la pantalla de asignación.", AlertType.ERROR);
        }
    }

    private void configurePostulationListView() {
        practitionerPostulationsListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectPostulation postulation, boolean isEmpty) {
                super.updateItem(postulation, isEmpty);
                if (isEmpty || postulation == null) {
                    setText(null);
                } else {
                    setText("Prioridad " + postulation.getPriorityLevel()
                            + " - " + postulation.getProjectName()
                            + " (" + postulation.getPostulationStatus() + ")");
                }
            }
        });
    }

    private void loadPractitionerPostulations() {
        try {
            postulations.clear();
            List<ProjectPostulation> retrievedPostulations =
                    postulationManager.retrievePractitionerPostulations(targetPractitionerId);
            postulations.addAll(retrievedPostulations);
        } catch (ManagerException e) {
            Controller.showAlert("Error de conexión", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleConfirmAssignmentAction() {
        ProjectPostulation selectedPostulation =
                practitionerPostulationsListView.getSelectionModel().getSelectedItem();

        if (selectedPostulation != null) {
            try {
                postulationManager.assignProjectToPractitioner(
                        targetPractitionerId, selectedPostulation.getProjectId());
                Controller.showAlert("Asignación Exitosa",
                        "El proyecto ha sido asignado al practicante correctamente.", AlertType.INFORMATION);
                store.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_PRACTITIONER_MENU));
            } catch (ManagerException e) {
                Controller.showAlert("Error en la asignación", e.getMessage(), AlertType.ERROR);
            }
        } else {
            Controller.showAlert("Selección requerida",
                    "Por favor, seleccione un proyecto de la lista para asignarlo.", AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnToDashboardAction(ActionEvent e) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_PRACTITIONER_MENU));
    }
}