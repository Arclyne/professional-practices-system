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

    private static final int ADMINISTRATOR_ID = 13;
    private static final int PRACTITIONER_ID = 123;
    private static final int PAGE_SIZE = 10;
    private static final int PAGE_OFFSET = 0;

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
        assertDoesNotThrow(() -> messageManager.sendMessage(ADMINISTRATOR_ID, "zS24242424@estudiantes.uv.mx",
                "Entrega de reporte mensual", "Le recuerdo entregar su reporte mensual antes del viernes."));
    }

    @Test
    void getInboxMessages_ValidId_ReturnsExpectedList() throws ManagerException {
        List<Message> expectedMessages = new ArrayList<>();

        List<Message> resultMessages = messageManager.getInboxMessages(PRACTITIONER_ID, PAGE_SIZE, PAGE_OFFSET);

        assertEquals(expectedMessages, resultMessages);
    }

    @Test
    void getSentMessages_ValidId_ReturnsExpectedList() throws ManagerException {
        List<Message> expectedMessages = new ArrayList<>();

        List<Message> resultMessages = messageManager.getSentMessages(ADMINISTRATOR_ID, PAGE_SIZE, PAGE_OFFSET);

        assertEquals(expectedMessages, resultMessages);
    }
}
