package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.CoordinatorManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

@Component
public class CoordinatorDetailsController {

    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label usernameLabel;
    @FXML private Label statusLabel;
    @FXML private Button inactivateButton;

    private final CoordinatorManager coordinatorManager;
    private final AppStore store;

    private int currentCoordinatorId;

    @Inject
    public CoordinatorDetailsController(CoordinatorManager coordinatorManager, AppStore store) {
        this.coordinatorManager = coordinatorManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        loadCurrentCoordinatorData();
    }

    private void loadCurrentCoordinatorData() {
        try {
            Coordinator currentCoordinator = coordinatorManager.retrieveCurrentCoordinator();

            if (currentCoordinator != null) {
                currentCoordinatorId = currentCoordinator.getId();
                nameLabel.setText(currentCoordinator.getName() + " " + currentCoordinator.getLastName());
                emailLabel.setText(currentCoordinator.getEmail());
                usernameLabel.setText(currentCoordinator.getUserName());
                statusLabel.setText(currentCoordinator.getStatus().getDatabaseValue());

                if (currentCoordinator.getStatus() == UserStatus.INACTIVE) {
                    inactivateButton.setDisable(true);
                    inactivateButton.setText("Coordinador Inactivo");
                }
            } else {
                Controller.showAlert("Sin resultados",
                        "No hay un coordinador activo o pendiente en el sistema actualmente.", AlertType.INFORMATION);
                inactivateButton.setDisable(true);
            }
        } catch (ManagerException e) {
            Controller.showAlert("Error de conexión",
                    "No se pudieron recuperar los datos del coordinador.", AlertType.ERROR);
        }
    }

    @FXML
    private void handleInactivateAction() {
        Dialog<ButtonType> confirmationDialog = new Dialog<>();
        confirmationDialog.setTitle("Confirmar Inactivación");
        confirmationDialog.setHeaderText("Acción Crítica: Inactivar Coordinador");

        Label warningLabel = new Label(
                "El coordinador pasará a estado 'No Activo' y perderá el acceso al sistema.\n" +
                        "Sus datos históricos se mantendrán intactos.");
        CheckBox confirmCheckBox = new CheckBox("Entiendo las consecuencias de esta acción.");
        confirmCheckBox.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");

        VBox dialogContent = new VBox(15);
        dialogContent.getChildren().addAll(warningLabel, confirmCheckBox);
        confirmationDialog.getDialogPane().setContent(dialogContent);
        confirmationDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Node confirmButton = confirmationDialog.getDialogPane().lookupButton(ButtonType.OK);
        confirmButton.setDisable(true);
        confirmCheckBox.selectedProperty().addListener((_, _, isChecked) -> confirmButton.setDisable(!isChecked));

        confirmationDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                executeInactivation();
            }
        });
    }

    private void executeInactivation() {
        try {
            coordinatorManager.inactivateCoordinator(currentCoordinatorId);
            Controller.showAlert("Éxito",
                    "El coordinador ha sido inactivado correctamente.", AlertType.INFORMATION);
            loadCurrentCoordinatorData();
        } catch (ManagerException e) {
            Controller.showAlert("Error en el proceso", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}