package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.AdminManager;
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
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class RegisterAdminController implements Initializable {

    @FXML private FormField fieldName;
    @FXML private FormField fieldLastName;
    @FXML private FormField fieldEmail;
    @FXML private FormField fieldPersonalNumber;
    @FXML private FormField fieldPassword;
    @FXML private FormComboBox comboBoxGender;

    private final AdminManager adminManager;
    private final AppStore store;

    @Inject
    public RegisterAdminController(AdminManager adminManager, AppStore store) {
        this.adminManager = adminManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue());
        comboBoxGender.setItems(genderOptions);
    }

    @FXML
    private void handleRegisterButtonAction() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos incompletos",
                    "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);
            return;
        }

        try {
            adminManager.registerInitialAdmin(buildAdministratorFromForm());
            Controller.showAlert("Configuración Exitosa",
                    "El administrador principal ha sido registrado. El sistema está listo para usarse.",
                    AlertType.INFORMATION);
            clearForm();
            store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
        } catch (ManagerException e) {
            Controller.showAlert("Error en la Configuración", e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean isFormIncomplete() {
        return fieldName.getText().isEmpty()
                || fieldLastName.getText().isEmpty()
                || fieldEmail.getText().isEmpty()
                || fieldPersonalNumber.getText().isEmpty()
                || fieldPassword.getText().isEmpty()
                || comboBoxGender.getValue() == null;
    }

    private Administrator buildAdministratorFromForm() {
        Administrator administrator = new Administrator();
        administrator.setName(fieldName.getText().trim());
        administrator.setLastName(fieldLastName.getText().trim());
        administrator.setEmail(fieldEmail.getText().trim());
        administrator.setUserName(fieldPersonalNumber.getText().trim());
        administrator.setPassword(fieldPassword.getText());
        administrator.setGender(Gender.fromDisplayValue((String) comboBoxGender.getValue()));
        administrator.setRole("Administrator");
        administrator.setStatus(UserStatus.ACTIVE);
        return administrator;
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }

    private void clearForm() {
        fieldName.setText("");
        fieldLastName.setText("");
        fieldEmail.setText("");
        fieldPersonalNumber.setText("");
        fieldPassword.setText("");
        comboBoxGender.clearSelection();
    }
}