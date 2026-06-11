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
import javafx.event.ActionEvent;
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
    }

    @FXML
    private void handleActionRegisterButton() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos incompletos",
                    "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);
            return;
        }

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

    private boolean isFormIncomplete() {
        return nameFormField.getText().isEmpty()
                || lastNameFormField.getText().isEmpty()
                || emailFormField.getText().isEmpty()
                || personalNumberFormField.getText().isEmpty()
                || genderFormComboBox.getValue() == null;
    }

    private Coordinator buildCoordinatorFromForm() {
        Coordinator coordinator = new Coordinator();
        coordinator.setName(nameFormField.getText().trim());
        coordinator.setLastName(lastNameFormField.getText().trim());
        coordinator.setEmail(emailFormField.getText().trim());
        coordinator.setUserName(personalNumberFormField.getText().trim());
        coordinator.setGender(Gender.fromDisplayValue((String) genderFormComboBox.getValue()));
        coordinator.setStatus(UserStatus.PENDING);
        return coordinator;
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}