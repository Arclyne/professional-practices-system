package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.StartSessionManager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

@Component
public class StartSessionController {

    private static final String CREDENTIAL_KEY_IDENTIFIER = "Identifier";
    private static final String CREDENTIAL_KEY_PASSWORD = "Password";

    @FXML private TextField textFieldUser;
    @FXML private PasswordField passwordFieldUser;
    @FXML private Button buttonClose;
    @FXML private Button buttonConnect;

    private final StartSessionManager startSessionManager;

    @Inject
    public StartSessionController(StartSessionManager startSessionManager) {
        this.startSessionManager = startSessionManager;
    }

    @FXML
    private void handleActionConnectButton() {
        String identifier = textFieldUser.getText().trim();
        String password = passwordFieldUser.getText().trim();

        if (identifier.isEmpty() || password.isEmpty()) {
            Controller.showErrorAlert("Campos requeridos",
                    "Por favor, ingrese su correo o matrícula y su contraseña.");
            return;
        }

        try {
            startSessionManager.processLogin(buildCredential(identifier, password));
        } catch (ManagerException e) {
            Controller.showAlert("Error al Iniciar Sesión", e.getMessage(), AlertType.ERROR);
        }
    }

    private Map<String, String> buildCredential(String identifier, String password) {
        Map<String, String> credential = new HashMap<>();
        credential.put(CREDENTIAL_KEY_IDENTIFIER, identifier);
        credential.put(CREDENTIAL_KEY_PASSWORD, password);
        return credential;
    }

    @FXML
    private void handleActionCloseButton() {
        Platform.exit();
        System.exit(0);
    }
}