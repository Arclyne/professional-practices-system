package mx.uv.fei.presentation.admin;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.people.CoordinatorManager;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

@Component
public class AdminHomeController {

    private static final String NO_ACTIVE_COORDINATOR_TITLE = "Sin coordinador activo";
    private static final String NO_ACTIVE_COORDINATOR_HINT =
            "Registra o activa un coordinador desde la sección Coordinación.";

    @FXML private Label activeCoordinatorNameLabel;
    @FXML private Label activeCoordinatorDetailLabel;

    private final CoordinatorManager coordinatorManager;

    @Inject
    public AdminHomeController(CoordinatorManager coordinatorManager) {
        this.coordinatorManager = coordinatorManager;
    }

    @FXML
    public void initialize() {
        try {
            Coordinator currentCoordinator = coordinatorManager.retrieveCurrentCoordinator();
            showCoordinatorSummary(currentCoordinator);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void showCoordinatorSummary(Coordinator currentCoordinator) {
        if (currentCoordinator != null) {
            activeCoordinatorNameLabel.setText(
                    currentCoordinator.getName() + " " + currentCoordinator.getLastName());
            activeCoordinatorDetailLabel.setText(
                    currentCoordinator.getEmail() + "  ·  " + currentCoordinator.getStatus().getDisplayLabel());
        } else {
            activeCoordinatorNameLabel.setText(NO_ACTIVE_COORDINATOR_TITLE);
            activeCoordinatorDetailLabel.setText(NO_ACTIVE_COORDINATOR_HINT);
        }
    }
}
