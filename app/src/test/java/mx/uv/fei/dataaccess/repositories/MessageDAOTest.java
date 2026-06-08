package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
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
        int generatedId = messageDAO.insertMessage("Asunto de prueba", "Cuerpo del mensaje de prueba.");
        assertTrue(generatedId > 0);
    }

    @Test
    void insertParticipant_ValidIds_ReturnsTrue() throws DAOException {
        int messageId = messageDAO.insertMessage("Asunto participante", "Cuerpo participante.");
        boolean isInserted = messageDAO.insertParticipant(messageId, SENDER_ID, RECEIVER_ID);
        assertTrue(isInserted);
    }

    @Test
    void getUserIdByEmail_ExistingEmail_ReturnsUserId() throws DAOException {
        int userId = messageDAO.getUserIdByEmail("adm@adm.com");
        assertEquals(SENDER_ID, userId);
    }

    @Test
    void getMessagesBySender_WithExistingMessages_ReturnsList() throws DAOException {
        int messageId = messageDAO.insertMessage("Asunto enviado", "Cuerpo enviado.");
        messageDAO.insertParticipant(messageId, SENDER_ID, RECEIVER_ID);
        List<Message> resultList = messageDAO.getMessagesBySender(SENDER_ID, 10, 0);
        assertFalse(resultList.isEmpty());
    }

    @Test
    void getMessagesByReceiver_WithExistingMessages_ReturnsList() throws DAOException {
        int messageId = messageDAO.insertMessage("Asunto recibido", "Cuerpo recibido.");
        messageDAO.insertParticipant(messageId, SENDER_ID, RECEIVER_ID);
        List<Message> resultList = messageDAO.getMessagesByReceiver(RECEIVER_ID, 10, 0);
        assertFalse(resultList.isEmpty());
    }

    @Test
    void insertParticipant_NonExistentMessageId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> {
            messageDAO.insertParticipant(9999, SENDER_ID, RECEIVER_ID);
        });
    }

    @Test
    void getUserIdByEmail_NonExistentEmail_ReturnsNegativeOne() throws DAOException {
        int userId = messageDAO.getUserIdByEmail("fantasma@uv.mx");
        assertEquals(-1, userId);
    }
}