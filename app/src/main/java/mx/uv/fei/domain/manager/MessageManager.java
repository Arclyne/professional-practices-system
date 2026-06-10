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
    private static final int MAXIMUM_SUBJECT_LENGTH = 255;
    private static final int MAXIMUM_BODY_LENGTH = 3000;

    private final MessageDAO messageDAO;
    private final SmtpEmailManager emailManager;

    @Inject
    public MessageManager(MessageDAO messageDAO, SmtpEmailManager emailManager) {
        this.messageDAO = messageDAO;
        this.emailManager = emailManager;
    }

    public void sendMessage(int senderId, String receiverEmail, String subject, String body) throws ManagerException {
        validateMessageLengths(subject, body);

        try {
            int receiverId = messageDAO.getUserIdByEmail(receiverEmail);
            if (receiverId <= 0) {
                throw new ManagerException("El correo proporcionado no pertenece a ningún usuario registrado en el sistema.");
            }

            int generatedMessageId = messageDAO.insertMessage(subject, body);
            if (generatedMessageId <= 0) {
                throw new ManagerException("No se pudo registrar el contenido del mensaje.");
            }

            boolean isParticipantInserted = messageDAO.insertParticipant(generatedMessageId, senderId, receiverId);
            if (!isParticipantInserted) {
                throw new ManagerException("El mensaje se creó, pero no se pudo vincular al remitente y destinatario.");
            }
        } catch (DAOException e) {
            log.error("Fallo al registrar el mensaje en la base de datos.", e);
            throw new ManagerException("No fue posible procesar el envío del mensaje.", e);
        }
    }

    public List<Message> getInboxMessages(int receiverId, int limit, int offset) throws ManagerException {
        try {
            return messageDAO.getMessagesByReceiver(receiverId, limit, offset);
        } catch (DAOException e) {
            log.error("Fallo al consultar la bandeja de entrada.", e);
            throw new ManagerException("No fue posible cargar los mensajes de la bandeja de entrada.", e);
        }
    }

    public List<Message> getSentMessages(int senderId, int limit, int offset) throws ManagerException {
        try {
            return messageDAO.getMessagesBySender(senderId, limit, offset);
        } catch (DAOException e) {
            log.error("Fallo al consultar los mensajes enviados.", e);
            throw new ManagerException("No fue posible cargar los mensajes enviados.", e);
        }
    }

    private void validateMessageLengths(String subject, String body) throws ManagerException {
        if (subject != null && subject.length() > MAXIMUM_SUBJECT_LENGTH) {
            throw new ManagerException("El asunto es demasiado grande. El límite máximo es de " + MAXIMUM_SUBJECT_LENGTH + " caracteres.");
        }
        if (body != null && body.length() > MAXIMUM_BODY_LENGTH) {
            throw new ManagerException("El cuerpo del mensaje es demasiado grande. El límite máximo es de " + MAXIMUM_BODY_LENGTH + " caracteres.");
        }
    }
}