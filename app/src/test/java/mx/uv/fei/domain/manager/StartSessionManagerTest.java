package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
import java.util.Map;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class StartSessionManagerTest {

    @Inject private IDatabaseConnection dbConnection;
    @Inject private StartSessionManager sessionManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void handleActionConnectButton_InvalidCredentials_ThrowsManagerException() {
        Map<String, String> creds = Map.of("Identifier", "bad@uv.mx", "Password", "wrong");
        assertThrows(Exception.class, () -> sessionManager.handleActionConnectButton(creds));
    }
}
