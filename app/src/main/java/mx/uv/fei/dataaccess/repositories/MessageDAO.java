package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IMessageDAO;
import mx.uv.fei.domain.dto.Message;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@Component
public class MessageDAO extends BaseDAO implements IMessageDAO {

    private static final String SQL_INSERT_MESSAGE = "INSERT INTO message (subject, body) VALUES (?, ?)";
    private static final String SQL_INSERT_PARTICIPANT = "INSERT INTO message_participant (message_id, sender_id, receiver_id) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ID_BY_EMAIL = "SELECT user_id FROM user WHERE email = ?";
    private static final String SQL_SELECT_BY_SENDER = "SELECT m.message_id, m.subject, m.body, m.send_date, " +
            "p.sender_id, p.receiver_id, CONCAT(u.name, ' ', u.last_name) AS display_name " +
            "FROM message m " +
            "INNER JOIN message_participant p ON m.message_id = p.message_id " +
            "INNER JOIN user u ON p.receiver_id = u.user_id " +
            "WHERE p.sender_id = ? " +
            "ORDER BY m.send_date DESC LIMIT ? OFFSET ?";

    private static final String SQL_SELECT_BY_RECEIVER = "SELECT m.message_id, m.subject, m.body, m.send_date, " +
            "p.sender_id, p.receiver_id, CONCAT(u.name, ' ', u.last_name) AS display_name " +
            "FROM message m " +
            "INNER JOIN message_participant p ON m.message_id = p.message_id " +
            "INNER JOIN user u ON p.sender_id = u.user_id " +
            "WHERE p.receiver_id = ? " +
            "ORDER BY m.send_date DESC LIMIT ? OFFSET ?";

    @Inject
    public MessageDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertMessage(String subject, String body) throws DAOException {
        int generatedIdentifier = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_MESSAGE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, subject);
            statement.setString(2, body);
            statement.executeUpdate();

            try (ResultSet generatedKeysResultSet = statement.getGeneratedKeys()) {
                if (generatedKeysResultSet.next()) {
                    generatedIdentifier = generatedKeysResultSet.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar registrar el mensaje en la base de datos.", e);
        }

        return generatedIdentifier;
    }

    @Override
    public boolean insertParticipant(int messageId, int senderId, int receiverId) throws DAOException {
        boolean isInserted = false;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PARTICIPANT)) {

            statement.setInt(1, messageId);
            statement.setInt(2, senderId);
            statement.setInt(3, receiverId);

            isInserted = statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error al registrar a los participantes del mensaje.", e);
        }

        return isInserted;
    }

    @Override
    public List<Message> getMessagesByReceiver(int receiverId, int limit, int offset) throws DAOException {
        List<Message> messagesList = new ArrayList<>();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_RECEIVER)) {

            statement.setInt(1, receiverId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Message message = mapResultSetToMessage(resultSet);
                    messagesList.add(message);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar la bandeja de entrada del usuario.", e);
        }

        return messagesList;
    }

    private Message mapResultSetToMessage(ResultSet resultSet) throws SQLException {
        Message message = new Message();
        message.setMessageId(resultSet.getInt("message_id"));
        message.setSubject(resultSet.getString("subject"));
        message.setBody(resultSet.getString("body"));
        message.setSendDate(resultSet.getTimestamp("send_date").toLocalDateTime());
        message.setSenderId(resultSet.getInt("sender_id"));
        message.setReceiverId(resultSet.getInt("receiver_id"));
        message.setSenderName(resultSet.getString("display_name"));

        return message;
    }

    @Override
    public int getUserIdByEmail(String email) throws DAOException {
        int userId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ID_BY_EMAIL)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    userId = resultSet.getInt("user_id");
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al buscar al destinatario por correo electrónico.", e);
        }

        return userId;
    }

    @Override
    public List<Message> getMessagesBySender(int senderId, int limit, int offset) throws DAOException {
        List<Message> messagesList = new ArrayList<>();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_SENDER)) {

            statement.setInt(1, senderId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Message message = mapResultSetToMessage(resultSet);
                    messagesList.add(message);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar los mensajes enviados en la base de datos.", e);
        }
        return messagesList;
    }
}
