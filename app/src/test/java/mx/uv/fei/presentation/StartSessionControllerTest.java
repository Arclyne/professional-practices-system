package mx.uv.fei.presentation;

import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.awt.GraphicsEnvironment;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.StartSessionManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

public class StartSessionControllerTest extends ApplicationTest {

    private static final String FXML_PATH = "/mx/uv/fei/presentation/startSession.fxml";
    private static final String VALID_IDENTIFIER = "zS24242424@estudiantes.uv.mx";
    private static final String VALID_PASSWORD = "PracticasUv2026";

    private final StartSessionManager startSessionManager = mock(StartSessionManager.class);

    @BeforeAll
    static void requireGraphicalDisplay() {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Se omiten las pruebas de interfaz porque no hay un entorno gráfico disponible.");
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
        loader.setControllerFactory(controllerType -> new StartSessionController(startSessionManager));
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    private void fillCredentials(String identifier, String password) {
        interact(() -> {
            lookup("#textFieldUser").queryAs(TextField.class).setText(identifier);
            lookup("#passwordFieldUser").queryAs(PasswordField.class).setText(password);
        });
    }

    private void clickConnectButton() {
        interact(() -> lookup("#buttonConnect").queryAs(Button.class).fire());
    }

    @Test
    void handleActionConnectButton_EmptyCredentials_DoesNotAttemptLogin() throws ManagerException {
        clickConnectButton();

        verify(startSessionManager, never()).handleActionConnectButton(anyMap());
    }

    @Test
    void handleActionConnectButton_CompleteCredentials_AttemptsLogin() throws ManagerException {
        fillCredentials(VALID_IDENTIFIER, VALID_PASSWORD);

        clickConnectButton();

        verify(startSessionManager).handleActionConnectButton(anyMap());
    }
}
