package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.manager.TokenManager;
import mx.uv.fei.domain.common.CommonControler;
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

            CommonControler.showSuccessAlert("Éxito", "Autenticación completada correctamente.");
            store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));

        } catch (ManagerException managerException) {
            // Ahora mostrará los mensajes exactos ("El token expiró", "Formato incorrecto", etc.)
            CommonControler.showErrorAlert("Validación fallida", managerException.getMessage());

        } catch (Exception exception) {
            // Respaldo de seguridad por si ocurre un NullPointerException o similar
            CommonControler.showErrorAlert("Error Fatal", "Ocurrió un error inesperado al verificar el token.");
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleActionSendButton(ActionEvent actionEvent) {
        try {
            tokenManager.generateToken();
            CommonControler.showSuccessAlert("Enviado",
                    "Se ha generado un nuevo código (por ahora no se envía al correo).");

        } catch (ManagerException managerException) {
            // Aquí te avisará explícitamente si "No hay una sesión activa"
            CommonControler.showErrorAlert("No se pudo generar", managerException.getMessage());

        } catch (Exception exception) {
            CommonControler.showErrorAlert("Error Fatal", "Ocurrió un error inesperado en el sistema.");
            exception.printStackTrace();
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        // En lugar de matar la ventana a la fuerza, limpiamos la sesión a medias
        // y le pedimos al Store que regrese a la pantalla de Login
        store.dispatch(new SessionAction.Logout());

        // Nota: Si SessionAction.Logout no redirige automáticamente en tu Reducer,
        // puedes despachar la navegación manualmente después del logout:
        // store.dispatch(new NavigationAction.GoToSection(AppSection.START_SESSION));
    }
}