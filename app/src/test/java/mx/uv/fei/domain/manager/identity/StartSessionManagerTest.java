package mx.uv.fei.domain.manager.identity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.Map;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class StartSessionManagerTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private StartSessionManager sessionManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void processLogin_InvalidCredentials_ThrowsManagerException() {
        Map<String, String> invalidCredentials = Map.of(
                "Identifier", "cuenta.inexistente@uv.mx",
                "Password", "ClaveIncorrecta99");

        assertThrows(ManagerException.class, () -> sessionManager.processLogin(invalidCredentials));
    }

    @Test
    void processLogin_ValidCredentialsAgainstHashedPassword_DoesNotThrow() {
        Map<String, String> validCredentials = Map.of(
                "Identifier", "rmarquez@uv.mx",
                "Password", "ClaveFei2026");

        assertDoesNotThrow(() -> sessionManager.processLogin(validCredentials));
    }

    @Test
    void processLogin_CorrectUserWrongPassword_ThrowsManagerException() {
        Map<String, String> wrongPassword = Map.of(
                "Identifier", "rmarquez@uv.mx",
                "Password", "ClaveIncorrecta99");

        assertThrows(ManagerException.class, () -> sessionManager.processLogin(wrongPassword));
    }
}
