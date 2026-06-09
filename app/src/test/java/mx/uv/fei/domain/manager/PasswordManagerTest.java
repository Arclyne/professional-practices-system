package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.sql.SQLException;
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
public class PasswordManagerTest {

    @Inject private IDatabaseConnection dbConnection;
    @Inject private PasswordManager passwordManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void updatePasswordAndActivate_NoSession_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> passwordManager.updatePasswordAndActivate("NewPass123", "NewPass123"));
    }
}
