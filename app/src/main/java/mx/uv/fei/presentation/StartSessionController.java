package mx.uv.fei.presentation;

import java.util.Map;
import java.util.HashMap;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.StartSessionManager;

@Component
public class StartSessionController {

    private final StartSessionManager startSessionManager;

    @FXML
    private TextField textFieldUser;
    @FXML
    private PasswordField passwordFieldUser;
    @FXML
    private Button buttonClose;
    @FXML
    private Button buttonConnect;

    @Inject
    public StartSessionController(StartSessionManager manager) {
        this.startSessionManager = manager;
    }

    @FXML
    private void handleActionConnectButton(ActionEvent actionEvent) {
        String userNameInput = textFieldUser.getText().trim();
        String userPasswordInput = passwordFieldUser.getText().trim();

        if (userNameInput.isEmpty() || userPasswordInput.isEmpty()) {
            CommonControler.showErrorAlert("Campos requeridos", "Por favor, ingrese su usuario y contraseña.");
            return;
        }

        Map<String, String> credential = new HashMap<>();
        credential.put("User", userNameInput);
        credential.put("Password", userPasswordInput);

        try {
            startSessionManager.handleActionConnectButton(credential, actionEvent);
        } catch (ManagerException e) {
            CommonControler.showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionCloseButton(ActionEvent actionEvent) {
        Platform.exit();
        System.exit(0);
    }
}