package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.domain.dto.Message;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import java.util.List;

public interface IMessageDAO {
    int insertMessage(String subject, String body) throws DAOException;
    void insertParticipant(int messageId, int senderId, int receiverId) throws DAOException;
    List<Message> getMessagesByReceiver(int receiverId, int limit, int offset) throws DAOException;
    int getUserIdByEmail(String email) throws DAOException;
    List<Message> getMessagesBySender(int senderId, int limit, int offset) throws DAOException;
}
