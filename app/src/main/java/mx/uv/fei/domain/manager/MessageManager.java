package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.repositories.MessageDAO;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.common.validators.FieldLengthLimits;
import mx.uv.fei.domain.dto.Message;
import mx.uv.fei.domain.dto.MessageRequest;
import mx.uv.fei.domain.exceptions.ManagerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class MessageManager {

    private static final Logger log = LoggerFactory.getLogger(MessageManager.class);

    private final MessageDAO messageDAO;
    private final SmtpEmailManager emailManager;

    @Inject
    public MessageManager(MessageDAO messageDAO, SmtpEmailManager emailManager) {
        this.messageDAO = messageDAO;
        this.emailManager = emailManager;
    }

    public void sendMessage(MessageRequest messageRequest) throws ManagerException {
        validateMessageRequest(messageRequest);

        try {
            int receiverId = messageDAO.getUserIdByEmail(messageRequest.receiverEmail());
            if (receiverId <= 0) {
                throw new ManagerException("El correo proporcionado no pertenece a ningún usuario registrado en el sistema.");
            }

            int generatedMessageId = messageDAO.insertMessage(messageRequest.subject(), messageRequest.body());
            if (generatedMessageId <= 0) {
                throw new ManagerException("No se pudo registrar el contenido del mensaje.");
            }

            messageDAO.insertParticipant(generatedMessageId, messageRequest.senderId(), receiverId);
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

    private void validateMessageRequest(MessageRequest messageRequest) throws ManagerException {
        BaseValidator.validateString(messageRequest.receiverEmail(),
                "El correo del destinatario es obligatorio.");
        BaseValidator.validateMaxLength(messageRequest.receiverEmail(), FieldLengthLimits.EMAIL_MAX,
                "El correo del destinatario no puede exceder " + FieldLengthLimits.EMAIL_MAX + " caracteres.");
        if (!BaseValidator.isValidEmail(messageRequest.receiverEmail())) {
            throw new ManagerException("El formato del correo del destinatario no es válido.");
        }
        BaseValidator.validateString(messageRequest.body(),
                "El cuerpo del mensaje es obligatorio.");
        BaseValidator.validateMaxLength(messageRequest.subject(), FieldLengthLimits.MESSAGE_SUBJECT_MAX,
                "El asunto no puede exceder " + FieldLengthLimits.MESSAGE_SUBJECT_MAX + " caracteres.");
        BaseValidator.validateMaxLength(messageRequest.body(), FieldLengthLimits.LONG_TEXT_MAX,
                "El cuerpo del mensaje no puede exceder " + FieldLengthLimits.LONG_TEXT_MAX + " caracteres.");
    }
}