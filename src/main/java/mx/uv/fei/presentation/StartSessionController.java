package mx.uv.fei.presentation;

import java.util.Map;
import java.util.HashMap;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.common.CommonValidator;
import mx.uv.fei.domain.manager.SceneManager;
import mx.uv.fei.domain.manager.StartSessionManager;

public class StartSessionController {

    private StartSessionManager startSessionManager;
    @FXML
    private TextField textFieldUser;

    @FXML
    private PasswordField passwordFieldUser;

    @FXML
    private Button buttonClose;

    @FXML
    private Button buttonConnect;

    public StartSessionController(StartSessionManager manger) {
        this.startSessionManager = manger;
    }

    @FXML
    private void handleActionConnectButton(ActionEvent actionEvent) {
        String userNameInput = textFieldUser.getText().trim();
        String userPasswordInput = passwordFieldUser.getText().trim();

        CommonValidator.validateNoEmptyString(userNameInput, "Introdusca un usuario");
        CommonValidator.validateNoEmptyString(userPasswordInput, "Introdusca una contraseña");

        Map<String, String> credential = new HashMap<>();
        credential.put("User", userNameInput);
        credential.put("Password", userPasswordInput);

        startSessionManager.handleActionConnectButton(credential, actionEvent);

        if (userNameInput.isEmpty() || userPasswordInput.isEmpty()) {
            CommonControler.showErrorAlert("Campos requeridos", "Por favor, ingrese su usuario y contraseña.");
            return;
        }

    }

    @FXML
    private void handleActionCloseButton(ActionEvent actionEvent) {
        SceneManager.closeCurrentWindow(actionEvent);
    }

}
