package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PasswordManager;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class PasswordResetController implements Initializable {

    @FXML private PasswordField passwordFieldNew;
    @FXML private PasswordField passwordFieldConfirm;
    @FXML private Button buttonSave;
    @FXML private Button buttonCancel;

    private final PasswordManager passwordManager;

    @Inject
    public PasswordResetController(PasswordManager passwordManager) {
        this.passwordManager = passwordManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void handleActionSaveButton() {
        try {
            passwordManager.updatePasswordAndActivate(
                    passwordFieldNew.getText(), passwordFieldConfirm.getText());
            Controller.showSuccessAlert("Activación Exitosa",
                    "Su contraseña ha sido guardada y su cuenta ahora está Activa.");
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de Validación", e.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        Node source = (Node) event.getSource();
        Stage stage = (Stage) source.getScene().getWindow();
        stage.close();
    }
}