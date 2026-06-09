package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class MessageManagerTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private MessageManager messageManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void sendMessage_ValidData_DoesNotThrow() {
        assertDoesNotThrow(() -> messageManager.sendMessage(13, "angel24@gmail.com", "Asunto", "Cuerpo"));
    }

    @Test
    void getInboxMessages_ValidId_ReturnsList() {
        assertDoesNotThrow(() -> messageManager.getInboxMessages(123, 10, 0));
    }

    @Test
    void getSentMessages_ValidId_ReturnsList() {
        assertDoesNotThrow(() -> messageManager.getSentMessages(13, 10, 0));
    }
}
