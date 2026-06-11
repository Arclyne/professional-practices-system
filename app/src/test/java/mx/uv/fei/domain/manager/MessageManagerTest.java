package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Message;
import mx.uv.fei.domain.dto.MessageRequest;
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
    private static final String PRACTITIONER_EMAIL = "zS24242424@estudiantes.uv.mx";
    private static final int SUBJECT_LIMIT = 255;
    private static final int BODY_LIMIT = 3000;

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
        MessageRequest validRequest = new MessageRequest(ADMINISTRATOR_ID, PRACTITIONER_EMAIL,
                "Entrega de reporte mensual", "Le recuerdo entregar su reporte mensual antes del viernes.");

        assertDoesNotThrow(() -> messageManager.sendMessage(validRequest));
    }

    @Test
    void sendMessage_InvalidEmailFormat_ThrowsManagerException() {
        MessageRequest invalidEmailRequest = new MessageRequest(ADMINISTRATOR_ID, "correo-sin-arroba",
                "Asunto", "Cuerpo del mensaje.");

        assertThrows(ManagerException.class, () -> messageManager.sendMessage(invalidEmailRequest));
    }

    @Test
    void sendMessage_EmptyBody_ThrowsManagerException() {
        MessageRequest emptyBodyRequest = new MessageRequest(ADMINISTRATOR_ID, PRACTITIONER_EMAIL,
                "Asunto", "   ");

        assertThrows(ManagerException.class, () -> messageManager.sendMessage(emptyBodyRequest));
    }

    @Test
    void sendMessage_SubjectExceedsLimit_ThrowsManagerException() {
        String oversizedSubject = "A".repeat(SUBJECT_LIMIT + 1);
        MessageRequest oversizedSubjectRequest = new MessageRequest(ADMINISTRATOR_ID, PRACTITIONER_EMAIL,
                oversizedSubject, "Cuerpo del mensaje.");

        assertThrows(ManagerException.class, () -> messageManager.sendMessage(oversizedSubjectRequest));
    }

    @Test
    void sendMessage_BodyExceedsLimit_ThrowsManagerException() {
        String oversizedBody = "A".repeat(BODY_LIMIT + 1);
        MessageRequest oversizedBodyRequest = new MessageRequest(ADMINISTRATOR_ID, PRACTITIONER_EMAIL,
                "Asunto", oversizedBody);

        assertThrows(ManagerException.class, () -> messageManager.sendMessage(oversizedBodyRequest));
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
