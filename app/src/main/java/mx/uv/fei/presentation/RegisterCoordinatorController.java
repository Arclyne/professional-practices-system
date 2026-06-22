package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.CoordinatorManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class RegisterCoordinatorController implements Initializable {

    @FXML private FormField nameFormField;
    @FXML private FormField lastNameFormField;
    @FXML private FormField emailFormField;
    @FXML private FormField personalNumberFormField;
    @FXML private FormComboBox genderFormComboBox;

    private final CoordinatorManager coordinatorManager;
    private final AppStore store;

    private Coordinator coordinatorBeingEdited;

    @Inject
    public RegisterCoordinatorController(CoordinatorManager coordinatorManager, AppStore store) {
        this.coordinatorManager = coordinatorManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue());
        genderFormComboBox.setItems(genderOptions);

        String selectedEntityId = store.getState().navigationState().targetEntityId();
        if (selectedEntityId != null && !selectedEntityId.isBlank()) {
            loadCoordinatorForEdit(Integer.parseInt(selectedEntityId));
        }
    }

    private void loadCoordinatorForEdit(int coordinatorId) {
        try {
            Coordinator coordinator = coordinatorManager.getCoordinatorById(coordinatorId);
            if (coordinator.getId() <= 0) {
                Controller.showAlert("Error de Carga",
                        "No se encontró la información del coordinador seleccionado.", AlertType.ERROR);
                return;
            }
            coordinatorBeingEdited = coordinator;
            nameFormField.setText(coordinator.getName());
            lastNameFormField.setText(coordinator.getLastName());
            emailFormField.setText(coordinator.getEmail());
            personalNumberFormField.setText(coordinator.getUserName());
            personalNumberFormField.setDisable(true);
            genderFormComboBox.valueProperty().set(coordinator.getGender().getDisplayValue());
        } catch (ManagerException e) {
            Controller.showAlert("Error de Carga", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionRegisterButton() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos incompletos",
                    "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);
            return;
        }

        if (coordinatorBeingEdited != null) {
            updateCoordinator();
        } else {
            registerCoordinator();
        }
    }

    private void registerCoordinator() {
        try {
            Coordinator coordinator = buildCoordinatorFromForm();
            String temporaryPassword = coordinatorManager.registerNewCoordinator(coordinator);
            Controller.showAlert("Registro Exitoso",
                    "El coordinador ha sido registrado correctamente en el sistema.\n"
                            + "Contraseña temporal generada: " + temporaryPassword,
                    AlertType.INFORMATION);
            store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
        } catch (ManagerException e) {
            Controller.showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    private void updateCoordinator() {
        try {
            applyEditableFields(coordinatorBeingEdited);
            coordinatorManager.updateCoordinator(coordinatorBeingEdited, coordinatorBeingEdited.getId());
            Controller.showAlert("Actualización Exitosa",
                    "La información del coordinador se actualizó correctamente.", AlertType.INFORMATION);
            store.dispatch(new NavigationAction.GoToSection(AppSection.COORDINATOR_MANAGEMENT_MENU));
        } catch (ManagerException e) {
            Controller.showAlert("Error al Actualizar", e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean isFormIncomplete() {
        return nameFormField.getText().isEmpty()
                || lastNameFormField.getText().isEmpty()
                || emailFormField.getText().isEmpty()
                || personalNumberFormField.getText().isEmpty()
                || genderFormComboBox.getValue() == null;
    }

    private Coordinator buildCoordinatorFromForm() {
        Coordinator coordinator = new Coordinator();
        applyEditableFields(coordinator);
        coordinator.setUserName(personalNumberFormField.getText().trim());
        coordinator.setStatus(UserStatus.PENDING);
        return coordinator;
    }

    private void applyEditableFields(Coordinator coordinator) {
        coordinator.setName(nameFormField.getText().trim());
        coordinator.setLastName(lastNameFormField.getText().trim());
        coordinator.setEmail(emailFormField.getText().trim());
        coordinator.setGender(Gender.fromDisplayValue((String) genderFormComboBox.getValue()));
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}
