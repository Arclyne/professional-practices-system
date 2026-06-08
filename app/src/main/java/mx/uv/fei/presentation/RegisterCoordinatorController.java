package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;

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

@Component
public class RegisterCoordinatorController implements Initializable {

    @FXML
    private FormField nameFormField;
    @FXML
    private FormField lastNameFormField;
    @FXML
    private FormField emailFormField;
    @FXML
    private FormField personalNumberFormField;
    @FXML
    private FormComboBox genderFormComboBox;

    private final CoordinatorManager coordinatorManager;
    private final AppStore applicationNavigationStore;

    @Inject
    public RegisterCoordinatorController(CoordinatorManager coordinatorManager, AppStore applicationNavigationStore) {
        this.coordinatorManager = coordinatorManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> genderOptionsObservableList = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue()
        );
        genderFormComboBox.setItems(genderOptionsObservableList);
    }

    @FXML
    private void handleActionRegisterButton(ActionEvent userActionEvent) {
        if (nameFormField.getText().isEmpty() || lastNameFormField.getText().isEmpty() ||
                emailFormField.getText().isEmpty() || personalNumberFormField.getText().isEmpty() ||
                genderFormComboBox.getValue() == null) {

            Controller.showAlert("Campos incompletos", "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);

        } else {
            try {
                Coordinator newCoordinatorInformation = getNewCoordinator();
                String assignedTemporaryPassword = coordinatorManager.registerNewCoordinator(newCoordinatorInformation);

                Controller.showAlert("Registro Exitoso",
                        "El coordinador ha sido registrado correctamente en el sistema.\nContraseña temporal generada: " + assignedTemporaryPassword,
                        AlertType.INFORMATION);

                applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));

            } catch (ManagerException registrationManagerException) {
                Controller.showAlert("Error en el Registro", registrationManagerException.getMessage(), AlertType.ERROR);
            }
        }
    }

    private Coordinator getNewCoordinator() {
        Coordinator mappedCoordinator = new Coordinator();

        mappedCoordinator.setName(nameFormField.getText().trim());
        mappedCoordinator.setLastName(lastNameFormField.getText().trim());
        mappedCoordinator.setEmail(emailFormField.getText().trim());
        mappedCoordinator.setUserName(personalNumberFormField.getText().trim());

        String selectedSex = (String) genderFormComboBox.getValue();
        mappedCoordinator.setGender(Gender.fromDisplayValue(selectedSex));

        mappedCoordinator.setStatus(UserStatus.PENDING);

        return mappedCoordinator;
    }

    @FXML
    private void handleActionCancelButton(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}