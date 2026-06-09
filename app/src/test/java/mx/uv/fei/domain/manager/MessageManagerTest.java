package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Message;
import mx.uv.fei.domain.exceptions.ManagerException;
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
    void getInboxMessages_ValidId_ReturnsExpectedList() throws ManagerException {
        List<Message> expectedList = new ArrayList<>();
        List<Message> resultList = messageManager.getInboxMessages(123, 10, 0);

        assertEquals(expectedList, resultList);
    }

    @Test
    void getSentMessages_ValidId_ReturnsExpectedList() throws ManagerException {
        List<Message> expectedList = new ArrayList<>();
        List<Message> resultList = messageManager.getSentMessages(13, 10, 0);

        assertEquals(expectedList, resultList);
    }
}