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
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
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
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> opcionesSexo = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue()
        );
        comboBoxSexo.setItems(opcionesSexo);
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        if (fieldNombre.getText().isEmpty() || fieldApellido.getText().isEmpty() ||
                fieldCorreo.getText().isEmpty() || fieldNoPersonal.getText().isEmpty() ||
                fieldPassword.getText().isEmpty() || comboBoxSexo.getValue() == null) {

            Controller.showAlert("Campos incompletos", "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);
            return;
        }

        Administrator newAdmin = createAdministrator();

        try {
            manager.registerInitialAdmin(newAdmin);

            Controller.showAlert("Configuración Exitosa",
                    "El administrador principal ha sido registrado. El sistema está listo para usarse.",
                    AlertType.INFORMATION);

            clearForm();
            store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));

        } catch (ManagerException e) {
            Controller.showAlert("Error en la Configuración", e.getMessage(), AlertType.ERROR);
        }
    }

    private Administrator createAdministrator() {
        Administrator newAdmin = new Administrator();
        newAdmin.setName(fieldNombre.getText().trim());
        newAdmin.setLastName(fieldApellido.getText().trim());
        newAdmin.setEmail(fieldCorreo.getText().trim());
        newAdmin.setUserName(fieldNoPersonal.getText().trim());
        newAdmin.setPassword(fieldPassword.getText());

        String selectedSex = (String) comboBoxSexo.getValue();
        newAdmin.setGender(Gender.fromDisplayValue(selectedSex));

        newAdmin.setRole("Administrator");

        newAdmin.setStatus(UserStatus.ACTIVE);

        return newAdmin;
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