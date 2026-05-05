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
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.dto.Coordinator;
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
    private FormField fieldPassword;
    @FXML
    private FormComboBox comboBoxSexo;

    private final CoordinatorManager manager;
    private final Store store;

    @Inject
    public RegisterCoordinatorController(CoordinatorManager manager, Store store) {

        this.manager = manager;
        this.store = store;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> opcionesSexo = FXCollections.observableArrayList(
                "Masculino", "Femenino", "Otro");
        comboBoxSexo.setItems(opcionesSexo);
    }

    @FXML
    private void handleActionRegisterButton(ActionEvent event) {
        if (fieldNombre.getText().isEmpty() || fieldApellido.getText().isEmpty() ||
                fieldCorreo.getText().isEmpty() || fieldNoPersonal.getText().isEmpty() ||
                fieldPassword.getText().isEmpty() || comboBoxSexo.getValue() == null) {

            CommonControler.showAlert("Campos incompletos",
                    "Por favor, llene todos los campos obligatorios.",
                    AlertType.WARNING);
            return;
        }

        Coordinator newCoordinator = getNewCoordinator();

        try {
            manager.registerNewCoordinator(newCoordinator);

            CommonControler.showAlert("Registro Exitoso",
                    "El coordinador ha sido registrado correctamente en el sistema.",
                    AlertType.INFORMATION);

            clearForm();

        } catch (ManagerException e) {
            CommonControler.showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    private Coordinator getNewCoordinator() {
        Coordinator newCoordinator = new Coordinator();
        newCoordinator.setName(fieldNombre.getText().trim());
        newCoordinator.setLastName(fieldApellido.getText().trim());
        newCoordinator.setEmail(fieldCorreo.getText().trim());
        newCoordinator.setUserName(fieldNoPersonal.getText().trim());
        newCoordinator.setPassword(fieldPassword.getText().trim());
        newCoordinator.setGender((String) comboBoxSexo.getValue());

        newCoordinator.setRole("Coordinator");
        newCoordinator.setStatus("Pendiente");
        return newCoordinator;
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }

    private void clearForm() {
        if (fieldNombre != null)
            fieldNombre.setText("");
        if (fieldApellido != null)
            fieldApellido.setText("");
        if (fieldCorreo != null)
            fieldCorreo.setText("");
        if (fieldNoPersonal != null)
            fieldNoPersonal.setText("");
        if (fieldPassword != null)
            fieldPassword.setText("");
        if (comboBoxSexo != null)
            comboBoxSexo.clearSelection();
    }
}