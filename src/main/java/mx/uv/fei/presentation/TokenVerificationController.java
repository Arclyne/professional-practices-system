package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import mx.uv.fei.domain.manager.SceneManager;
import mx.uv.fei.domain.manager.TokenManager;
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.net.URL;
import java.util.ResourceBundle;

public class TokenVerificationController implements Initializable {

    private final TokenManager tokenManager;

    @FXML
    private TextField textFieldToken;

    @FXML
    private Button buttonVerify;
    @FXML
    private Button buttonCancel;
    @FXML
    private Button buttonSend;

    public TokenVerificationController(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
    }

    @FXML
    private void handleActionVerifyButton(ActionEvent actionEvent) {
        try {
            tokenManager.verifyToken(textFieldToken.getText());

            CommonControler.showSuccessAlert("Éxito", "Autenticación completada correctamente.");
            SceneManager.closeCurrentWindow(actionEvent);

        } catch (ManagerException managerException) {
            CommonControler.showErrorAlert("Error de Autenticación", managerException.getMessage());
        }
    }

    @FXML
    private void handleActionSendButton(ActionEvent actionEvent) {
        try {
            tokenManager.generateToken();
            CommonControler.showSuccessAlert("Enviado",
                    "Se ha generado un nuevo código (por ahora no se envía al correo).");
        } catch (Exception exception) {
            CommonControler.showErrorAlert("Error", "No se pudo generar el token.");
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent actionEvent) {
        SceneManager.closeCurrentWindow(actionEvent);
    }
}