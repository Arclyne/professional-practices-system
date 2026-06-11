package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IMessageDAO;
import mx.uv.fei.domain.dto.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class MessageDAOTest {

    private static final int SENDER_ID = 13;
    private static final int RECEIVER_ID = 123;
    private static final String SENDER_FULL_NAME = "Ricardo Marquez Sosa";
    private static final String RECEIVER_FULL_NAME = "Angel Gabriel Aguilar Hernandez";
    private static final int PAGE_SIZE = 10;
    private static final int PAGE_OFFSET = 0;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IMessageDAO messageDAO;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void insertMessage_ValidSubjectAndBody_ReturnsGeneratedId() throws DAOException {
        int generatedId = messageDAO.insertMessage("Entrega de reporte mensual",
                "Le recuerdo entregar su reporte mensual antes del viernes.");

        assertTrue(generatedId > 0);
    }

    @Test
    void insertParticipant_ValidIds_ReturnsTrue() throws DAOException {
        int messageId = messageDAO.insertMessage("Revision de bitacora",
                "Su bitacora de actividades fue revisada por el academico.");

        boolean isInserted = messageDAO.insertParticipant(messageId, SENDER_ID, RECEIVER_ID);

        assertTrue(isInserted);
    }

    @Test
    void getUserIdByEmail_ExistingEmail_ReturnsUserId() throws DAOException {
        int userId = messageDAO.getUserIdByEmail("rmarquez@uv.mx");

        assertEquals(SENDER_ID, userId);
    }

    @Test
    void getMessagesBySender_WithExistingMessages_ReturnsExpectedList() throws DAOException {
        int messageId = messageDAO.insertMessage("Asignacion de proyecto",
                "Su proyecto de practicas fue asignado correctamente.");
        messageDAO.insertParticipant(messageId, SENDER_ID, RECEIVER_ID);

        List<Message> resultMessages = messageDAO.getMessagesBySender(SENDER_ID, PAGE_SIZE, PAGE_OFFSET);

        List<Message> expectedMessages = new ArrayList<>();
        Message expectedMessage = new Message();
        expectedMessage.setMessageId(messageId);
        expectedMessage.setSubject("Asignacion de proyecto");
        expectedMessage.setBody("Su proyecto de practicas fue asignado correctamente.");
        expectedMessage.setSenderId(SENDER_ID);
        expectedMessage.setReceiverId(RECEIVER_ID);
        expectedMessage.setSenderName(RECEIVER_FULL_NAME);
        if (!resultMessages.isEmpty()) {
            expectedMessage.setSendDate(resultMessages.get(0).getSendDate());
        }
        expectedMessages.add(expectedMessage);
        assertEquals(expectedMessages, resultMessages);
    }

    @Test
    void getMessagesByReceiver_WithExistingMessages_ReturnsExpectedList() throws DAOException {
        int messageId = messageDAO.insertMessage("Resultado de evaluacion",
                "Su reporte mensual de mayo fue evaluado satisfactoriamente.");
        messageDAO.insertParticipant(messageId, SENDER_ID, RECEIVER_ID);

        List<Message> resultMessages = messageDAO.getMessagesByReceiver(RECEIVER_ID, PAGE_SIZE, PAGE_OFFSET);

        List<Message> expectedMessages = new ArrayList<>();
        Message expectedMessage = new Message();
        expectedMessage.setMessageId(messageId);
        expectedMessage.setSubject("Resultado de evaluacion");
        expectedMessage.setBody("Su reporte mensual de mayo fue evaluado satisfactoriamente.");
        expectedMessage.setSenderId(SENDER_ID);
        expectedMessage.setReceiverId(RECEIVER_ID);
        expectedMessage.setSenderName(SENDER_FULL_NAME);
        if (!resultMessages.isEmpty()) {
            expectedMessage.setSendDate(resultMessages.get(0).getSendDate());
        }
        expectedMessages.add(expectedMessage);
        assertEquals(expectedMessages, resultMessages);
    }

    @Test
    void insertParticipant_NonExistentMessageId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> messageDAO.insertParticipant(9999, SENDER_ID, RECEIVER_ID));
    }

    @Test
    void getUserIdByEmail_NonExistentEmail_ReturnsNegativeOne() throws DAOException {
        int userId = messageDAO.getUserIdByEmail("correo.inexistente@uv.mx");

        assertEquals(-1, userId);
    }
}
