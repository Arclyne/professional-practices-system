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
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.AdminManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class RegisterAdminController implements Initializable {

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

    private final AdminManager manager;
    private final Store store;

    @Inject
    public RegisterAdminController(AdminManager manager, Store store) {
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
    private void handleRegisterButtonAction(ActionEvent event) {
        if (fieldNombre.getText().isEmpty() || fieldApellido.getText().isEmpty() ||
                fieldCorreo.getText().isEmpty() || fieldNoPersonal.getText().isEmpty() ||
                fieldPassword.getText().isEmpty() || comboBoxSexo.getValue() == null) {

            CommonControler.showAlert("Campos incompletos",
                    "Por favor, llene todos los campos obligatorios para configurar el sistema.",
                    AlertType.WARNING);
            return;
        }

        Administrator newAdmin = new Administrator();
        newAdmin.setName(fieldNombre.getText());
        newAdmin.setLastName(fieldApellido.getText());
        newAdmin.setEmail(fieldCorreo.getText());
        newAdmin.setUserName(fieldNoPersonal.getText());
        newAdmin.setPassword(fieldPassword.getText());
        newAdmin.setGender((String) comboBoxSexo.getValue());
        newAdmin.setRole("Administrador");

        try {
            manager.registerInitialAdmin(newAdmin);

            CommonControler.showAlert("Configuración Exitosa",
                    "El administrador principal ha sido registrado. El sistema está listo para usarse.",
                    AlertType.INFORMATION);

            clearForm();
            store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));

        } catch (ManagerException e) {
            CommonControler.showAlert("Error en la Configuración", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
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