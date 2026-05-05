package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.manager.TokenManager;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.actions.SessionAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class TokenVerificationController implements Initializable {

    private final TokenManager tokenManager;
    private final Store store;

    @FXML
    private TextField textFieldToken;

    @FXML
    private Button buttonVerify;
    @FXML
    private Button buttonCancel;
    @FXML
    private Button buttonSend;

    @Inject
    public TokenVerificationController(TokenManager tokenManager, Store store) {
        this.tokenManager = tokenManager;
        this.store = store;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
    }

    @FXML
    private void handleActionVerifyButton(ActionEvent actionEvent) {
        try {
            tokenManager.verifyToken(textFieldToken.getText());

            Controller.showSuccessAlert("Éxito", "Autenticación completada correctamente.");
            store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));

        } catch (ManagerException managerException) {
            Controller.showErrorAlert("Validación fallida", managerException.getMessage());

        } catch (Exception exception) {
            Controller.showErrorAlert("Error Fatal", "Ocurrió un error inesperado al verificar el token.");
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleActionSendButton(ActionEvent actionEvent) {
        try {
            tokenManager.generateToken();
            Controller.showSuccessAlert("Enviado",
                    "Se ha generado un nuevo código (por ahora no se envía al correo).");

        } catch (ManagerException managerException) {
            Controller.showErrorAlert("No se pudo generar", managerException.getMessage());

        } catch (Exception exception) {
            Controller.showErrorAlert("Error Fatal", "Ocurrió un error inesperado en el sistema.");
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new SessionAction.Logout());
    }
}