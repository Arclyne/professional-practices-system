package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;

import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.CoordinatorManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

public class RegisterCoordinatorController implements Initializable {

    @FXML
    private FormField fieldNombre;
    @FXML
    private FormField fieldApellido;
    @FXML
    private FormField fieldCorreo;
    @FXML
    private FormField fieldNoPersonal;
    @FXML
    private FormComboBox comboBoxSexo;

    private final CoordinatorManager coordinatorManager;
    private final Store applicationNavigationStore;

    @Inject
    public RegisterCoordinatorController(CoordinatorManager coordinatorManager, Store applicationNavigationStore) {
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
        comboBoxSexo.setItems(genderOptionsObservableList);
    }

    @FXML
    private void handleActionRegisterButton(ActionEvent userActionEvent) {
        if (fieldNombre.getText().isEmpty() || fieldApellido.getText().isEmpty() ||
                fieldCorreo.getText().isEmpty() || fieldNoPersonal.getText().isEmpty() ||
                comboBoxSexo.getValue() == null) {

            Controller.showAlert("Campos incompletos", "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);

        } else {
            Coordinator newCoordinatorInformation = getNewCoordinator();

            try {
                String assignedTemporaryPassword = coordinatorManager.registerNewCoordinator(newCoordinatorInformation);

                Controller.showAlert("Registro Exitoso",
                        "El coordinador ha sido registrado correctamente en el sistema.\nContraseña temporal generada: " + assignedTemporaryPassword,
                        AlertType.INFORMATION);

                clearForm();

            } catch (ManagerException registrationManagerException) {
                Controller.showAlert("Error en el Registro", registrationManagerException.getMessage(), AlertType.ERROR);
            }
        }
    }

    private Coordinator getNewCoordinator() {
        Coordinator mappedCoordinator = new Coordinator();
        mappedCoordinator.setName(fieldNombre.getText().trim());
        mappedCoordinator.setLastName(fieldApellido.getText().trim());
        mappedCoordinator.setEmail(fieldCorreo.getText().trim());
        mappedCoordinator.setUserName(fieldNoPersonal.getText().trim());

        String selectedSex = (String) comboBoxSexo.getValue();
        mappedCoordinator.setGender(Gender.fromDisplayValue(selectedSex));

        mappedCoordinator.setRole("Coordinator");

        mappedCoordinator.setStatus(UserStatus.PENDING);

        return mappedCoordinator;
    }

    @FXML
    private void handleActionCancelButton(ActionEvent userActionEvent) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }

    private void clearForm() {
        if (fieldNombre != null) {
            fieldNombre.setText("");
        }
        if (fieldApellido != null) {
            fieldApellido.setText("");
        }
        if (fieldCorreo != null) {
            fieldCorreo.setText("");
        }
        if (fieldNoPersonal != null) {
            fieldNoPersonal.setText("");
        }
        if (comboBoxSexo != null) {
            comboBoxSexo.clearSelection();
        }
    }
}