package mx.uv.fei.presentation.admin;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.people.CoordinatorManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class RegisterCoordinatorController implements Initializable {

    @FXML private FormField nameFormField;
    @FXML private FormField lastNameFormField;
    @FXML private FormField emailFormField;
    @FXML private FormField personalNumberFormField;
    @FXML private FormComboBox genderFormComboBox;
    @FXML private Button registerButton;

    private final CoordinatorManager coordinatorManager;
    private final ShellNavigator shellNavigator;

    private Coordinator coordinatorBeingEdited;

    @Inject
    public RegisterCoordinatorController(CoordinatorManager coordinatorManager, ShellNavigator shellNavigator) {
        this.coordinatorManager = coordinatorManager;
        this.shellNavigator = shellNavigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue());
        genderFormComboBox.setItems(genderOptions);

        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof Coordinator) {
            coordinatorBeingEdited = (Coordinator) pendingEntity;
            populateFormForEdit(coordinatorBeingEdited);
        } else {
            coordinatorBeingEdited = null;
            registerButton.setText("Registrar Coordinador");
        }
    }

    private void populateFormForEdit(Coordinator coordinator) {
        registerButton.setText("Guardar cambios");
        nameFormField.setText(coordinator.getName());
        lastNameFormField.setText(coordinator.getLastName());
        emailFormField.setText(coordinator.getEmail());
        personalNumberFormField.setText(coordinator.getUserName());
        personalNumberFormField.setDisable(true);
        if (coordinator.getGender() != null) {
            genderFormComboBox.valueProperty().set(coordinator.getGender().getDisplayValue());
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
            shellNavigator.returnToList();
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
            shellNavigator.returnToList();
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
        shellNavigator.returnToList();
    }
}
