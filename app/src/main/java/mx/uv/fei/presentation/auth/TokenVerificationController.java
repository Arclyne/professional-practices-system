package mx.uv.fei.presentation.auth;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.identity.TokenManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.actions.SessionAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class TokenVerificationController implements Initializable {

    @FXML private TextField textFieldToken;
    @FXML private Button buttonVerify;
    @FXML private Button buttonCancel;
    @FXML private Button buttonSend;

    private final TokenManager tokenManager;
    private final AppStore store;

    @Inject
    public TokenVerificationController(TokenManager tokenManager, AppStore store) {
        this.tokenManager = tokenManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void handleActionVerifyButton() {
        try {
            tokenManager.verifyToken(textFieldToken.getText());
            Controller.showSuccessAlert("Éxito", "Autenticación completada correctamente.");
            store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
        } catch (ManagerException e) {
            Controller.showErrorAlert("Validación fallida", e.getMessage());
        }
    }

    @FXML
    private void handleActionSendButton() {
        try {
            tokenManager.generateToken();
            Controller.showSuccessAlert("Enviado",
                    "Se ha generado un nuevo código (por ahora no se envía al correo).");
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo generar", e.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new SessionAction.Logout());
        store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
    }
}