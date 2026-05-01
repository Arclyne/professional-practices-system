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
    private void handleActionCancelButton(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }
}