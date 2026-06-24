package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.ProjectPostulation;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PostulationManager;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
    private final ShellNavigator shellNavigator;
    private final ObservableList<ProjectPostulation> postulations = FXCollections.observableArrayList();

    private int targetPractitionerId;

    @Inject
    public AssignProjectController(PostulationManager postulationManager, ShellNavigator shellNavigator) {
        this.postulationManager = postulationManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        Object pendingEntity = shellNavigator.consumePendingEntity();

        if (pendingEntity instanceof Practitioner selectedPractitioner) {
            targetPractitionerId = selectedPractitioner.getId();
            practitionerInformationLabel.setText("Practicante: " + selectedPractitioner.getName() + " "
                    + selectedPractitioner.getLastName() + " (" + selectedPractitioner.getEnrollment() + ")");
            configurePostulationListView();
            practitionerPostulationsListView.setItems(postulations);
            loadPractitionerPostulations();
        } else {
            Controller.showAlert("Información faltante",
                    "No se pudo recuperar la información del practicante seleccionado.", AlertType.WARNING);
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
                shellNavigator.returnToList();
            } catch (ManagerException e) {
                Controller.showAlert("Error en la asignación", e.getMessage(), AlertType.ERROR);
            }
        } else {
            Controller.showAlert("Selección requerida",
                    "Por favor, seleccione un proyecto de la lista para asignarlo.", AlertType.WARNING);
        }
    }

    @FXML
    private void handleReturnToDashboardAction() {
        shellNavigator.returnToList();
    }
}