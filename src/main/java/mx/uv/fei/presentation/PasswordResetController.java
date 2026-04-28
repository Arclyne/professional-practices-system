package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

import mx.uv.fei.domain.manager.PasswordResetManager;
import mx.uv.fei.domain.manager.SceneManager;
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordResetController implements Initializable {

    private final PasswordResetManager passwordResetManager;

    @FXML
    private PasswordField passwordFieldNew;
    @FXML
    private PasswordField passwordFieldConfirm;

    @FXML
    private Button buttonSave;
    @FXML
    private Button buttonCancel;

    public PasswordResetController(PasswordResetManager passwordResetManager) {
        this.passwordResetManager = passwordResetManager;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
    }

    @FXML
    private void handleActionSaveButton(ActionEvent actionEvent) {
        try {
            passwordResetManager.updatePasswordAndActivate(
                    passwordFieldNew.getText(),
                    passwordFieldConfirm.getText());

            CommonControler.showSuccessAlert("Activación Exitosa",
                    "Su contraseña ha sido guardada y su cuenta ahora está Activa.");
            SceneManager.closeCurrentWindow(actionEvent);

        } catch (ManagerException managerException) {
            CommonControler.showErrorAlert("Error de Validación", managerException.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent actionEvent) {
        SceneManager.closeCurrentWindow(actionEvent);
    }
}