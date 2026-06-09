package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import mx.uv.fei.domain.dto.AuthenticationToken;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IAuthenticationToken;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;

@Component
public class AuthenticationTokenDAO extends BaseDAO implements IAuthenticationToken {

    private static final String SQL_INSERT = "INSERT INTO access_token (token_value, creation_time, username) VALUES (?, ?, ?)";
    private static final String SQL_SELECT = "SELECT token_value, creation_time, username FROM access_token WHERE token_value = ?";
    private static final String SQL_SELECT_CREATION_TIME = "SELECT creation_time FROM access_token WHERE token_value = ? AND username = ?";

    @Inject
    public AuthenticationTokenDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public boolean insertToken(AuthenticationToken tokenToInsert) throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {

            statement.setInt(1, tokenToInsert.getValueToken());
            statement.setTimestamp(2, Timestamp.valueOf(tokenToInsert.getTimeCreation()));
            statement.setString(3, tokenToInsert.getUserName());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            String debugMessage = String.format("Fallo al insertar token [%d] para el usuario '%s'. SQLState: %s, ErrorCode: %d",
                    tokenToInsert.getValueToken(), tokenToInsert.getUserName(), e.getSQLState(), e.getErrorCode());
            throw new DAOException(debugMessage, e);
        }
    }

    @Override
    public AuthenticationToken recoverToken(int tokenValue) throws DAOException {
        AuthenticationToken tokenRecovered = new AuthenticationToken();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT)) {

            statement.setInt(1, tokenValue);
            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    tokenRecovered.setValueToken(resultSet.getInt("token_value"));

                    Timestamp ts = resultSet.getTimestamp("creation_time");
                    if (ts != null) {
                        tokenRecovered.setTimeCreation(ts.toLocalDateTime());
                    }

                    tokenRecovered.setUserName(resultSet.getString("username"));
                }
            }
        } catch (SQLException e) {
            String debugMsg = String.format("Fallo al recuperar los datos del token [%d]. SQLState: %s, ErrorCode: %d",
                    tokenValue, e.getSQLState(), e.getErrorCode());
            throw new DAOException(debugMsg, e);
        }
        return tokenRecovered;
    }

    @Override
    public LocalDateTime getTokenCreationTime(int tokenValue, String userName) throws DAOException {
        LocalDateTime creationTime = null;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_CREATION_TIME)) {

            statement.setInt(1, tokenValue);
            statement.setString(2, userName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Timestamp ts = resultSet.getTimestamp("creation_time");
                    if (ts != null) {
                        creationTime = ts.toLocalDateTime();
                    }
                }
            }
        } catch (SQLException e) {
            String debugMsg = String.format("Fallo al consultar tiempo de creación para el token [%d] del usuario '%s'. SQLState: %s, ErrorCode: %d",
                    tokenValue, userName, e.getSQLState(), e.getErrorCode());
            throw new DAOException(debugMsg, e);
        }
        return creationTime;
    }

}
