package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.awt.GraphicsEnvironment;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.TokenManager;
import mx.uv.fei.domain.statemachine.AppStore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class TokenVerificationControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/tokenVerification.fxml";
    private static final String VALID_TOKEN = "123456";

    private final TokenManager tokenManager = mock(TokenManager.class);
    private final AppStore appStore = mock(AppStore.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new TokenVerificationController(tokenManager, appStore));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private void writeToken(String token) {
        interact(() -> lookup("#textFieldToken").queryAs(TextField.class).setText(token));
    }

    private void clickVerifyButton() {
        interact(() -> lookup("#buttonVerify").queryAs(Button.class).fire());
    }

    @Test
    void handleActionVerifyButton_ValidToken_NavigatesToDashboard() throws ManagerException {
        writeToken(VALID_TOKEN);

        clickVerifyButton();

        verify(tokenManager).verifyToken(VALID_TOKEN);
        verify(appStore).dispatch(any());
    }

    @Test
    void handleActionVerifyButton_InvalidToken_DoesNotNavigate() throws ManagerException {
        doThrow(new ManagerException("El token ingresado no es válido."))
                .when(tokenManager).verifyToken(anyString());
        writeToken("000000");

        clickVerifyButton();

        verify(appStore, never()).dispatch(any());
    }

    @Test
    void handleActionSendButton_WhenClicked_GeneratesToken() throws ManagerException {
        interact(() -> lookup("#buttonSend").queryAs(Button.class).fire());

        verify(tokenManager).generateToken();
    }
}
