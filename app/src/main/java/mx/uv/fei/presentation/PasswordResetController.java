package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.manager.PasswordManager;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class PasswordResetController implements Initializable {

    private final PasswordManager passwordManager;

    @FXML
    private PasswordField passwordFieldNew;
    @FXML
    private PasswordField passwordFieldConfirm;

    @FXML
    private Button buttonSave;
    @FXML
    private Button buttonCancel;

    @Inject
    public PasswordResetController(PasswordManager passwordResetMa) {
        this.passwordManager = passwordResetMa;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
    }

    @FXML
    private void handleActionSaveButton(ActionEvent actionEvent) {
        try {
            passwordManager.updatePasswordAndActivate(
                    passwordFieldNew.getText(),
                    passwordFieldConfirm.getText());

            Controller.showSuccessAlert("Activación Exitosa",
                    "Su contraseña ha sido guardada y su cuenta ahora está Activa.");

        } catch (ManagerException managerException) {
            Controller.showErrorAlert("Error de Validación", managerException.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }
}