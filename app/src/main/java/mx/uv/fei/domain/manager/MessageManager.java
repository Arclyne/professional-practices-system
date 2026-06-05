package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.repositories.MessageDAO;
import mx.uv.fei.domain.dto.Message;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class MessageManager {

    private static final Logger log = LoggerFactory.getLogger(MessageManager.class);
    private final MessageDAO messageDAO;
    private final SmtpEmailManager emailManager;

    private static final int MAX_SUBJECT_LENGTH = 255;
    private static final int MAX_BODY_LENGTH = 3000;

    private static final String MSG_SUBJECT_TOO_LONG = "El asunto es demasiado grande. El límite máximo es de " + MAX_SUBJECT_LENGTH + " caracteres.";
    private static final String MSG_BODY_TOO_LONG = "El cuerpo del mensaje es demasiado grande. El límite máximo es de " + MAX_BODY_LENGTH + " caracteres.";
    private static final String MSG_INVALID_EMAIL = "El correo proporcionado no pertenece a ningún usuario registrado en el sistema.";
    private static final String MSG_INSERT_ERROR = "No se pudo registrar el contenido del mensaje.";
    private static final String MSG_PARTICIPANT_ERROR = "El mensaje se creó, pero no se pudo vincular al remitente y destinatario.";
    private static final String MSG_DB_PROCESS_ERROR = "No fue posible procesar el envío del mensaje. Causa: ";

    @Inject
    public MessageManager(MessageDAO messageDAO, SmtpEmailManager emailManager) {
        this.messageDAO = messageDAO;
        this.emailManager = emailManager;
    }

    public void sendMessage(int senderId, String receiverEmail, String subject, String body) throws ManagerException {

        if (subject != null && subject.length() > MAX_SUBJECT_LENGTH) {
            throw new ManagerException(MSG_SUBJECT_TOO_LONG);
        }

        if (body != null && body.length() > MAX_BODY_LENGTH) {
            throw new ManagerException(MSG_BODY_TOO_LONG);
        }

        try {
            int receiverId = messageDAO.getUserIdByEmail(receiverEmail);

            if (receiverId <= 0) {
                throw new ManagerException(MSG_INVALID_EMAIL);
            }

            int messageIdentifier = messageDAO.insertMessage(subject, body);

            if (messageIdentifier <= 0) {
                throw new ManagerException(MSG_INSERT_ERROR);
            }

            boolean isParticipantInserted = messageDAO.insertParticipant(messageIdentifier, senderId, receiverId);
            if (!isParticipantInserted) {
                throw new ManagerException(MSG_PARTICIPANT_ERROR);
            }

        } catch (DAOException exception) {
            log.error("Fallo al registrar el mensaje en la base de datos", exception);
            throw new ManagerException(MSG_DB_PROCESS_ERROR + exception.getMessage(), exception);
        }
    }

    public List<Message> getInboxMessages(int receiverId, int limit, int offset) throws ManagerException {
        try {
            return messageDAO.getMessagesByReceiver(receiverId, limit, offset);
        } catch (DAOException exception) {
            log.error("Fallo al consultar la bandeja de entrada en la base de datos", exception);
            throw new ManagerException("No fue posible cargar los mensajes de la bandeja de entrada.", exception);
        }
    }

    public List<Message> getSentMessages(int senderId, int limit, int offset) throws ManagerException {
        try {
            return messageDAO.getMessagesBySender(senderId, limit, offset);
        } catch (DAOException exception) {
            log.error("Fallo al consultar los mensajes enviados en la base de datos", exception);
            throw new ManagerException("No fue posible cargar los mensajes enviados.", exception);
        }
    }
}