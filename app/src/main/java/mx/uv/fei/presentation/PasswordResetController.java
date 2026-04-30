package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

import mx.uv.fei.domain.manager.PasswordManager;
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.net.URL;
import java.util.ResourceBundle;

public class PasswordResetController implements Initializable {

    private final PasswordManager passwordResetMa;

    @FXML
    private PasswordField passwordFieldNew;
    @FXML
    private PasswordField passwordFieldConfirm;

    @FXML
    private Button buttonSave;
    @FXML
    private Button buttonCancel;

    public PasswordResetController(PasswordManager passwordResetMa) {
        this.passwordResetMa = passwordResetMa;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
    }

    @FXML
    private void handleActionSaveButton(ActionEvent actionEvent) {
        try {
            passwordResetMa.updatePasswordAndActivate(
                    passwordFieldNew.getText(),
                    passwordFieldConfirm.getText());

            CommonControler.showSuccessAlert("Activación Exitosa",
                    "Su contraseña ha sido guardada y su cuenta ahora está Activa.");

        } catch (ManagerException managerException) {
            CommonControler.showErrorAlert("Error de Validación", managerException.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }
}