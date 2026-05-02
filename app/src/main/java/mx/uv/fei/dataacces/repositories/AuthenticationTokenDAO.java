package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import mx.uv.fei.domain.dto.AuthenticationToken;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IAuthenticationToken;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

@Component
public class AuthenticationTokenDAO extends BaseDAO implements IAuthenticationToken {

    private static final String SQL_INSERT = "INSERT INTO ACCESS_TOKEN (TOKEN_VALUE, CREATION_TIME, NOMBRE_USUARIO) VALUES (?, ?, ?)";
    private static final String SQL_SELECT = "SELECT TOKEN_VALUE, CREATION_TIME, NOMBRE_USUARIO FROM ACCESS_TOKEN WHERE TOKEN_VALUE = ?";
    private static final String SQL_SELECT_CREATION_TIME = "SELECT CREATION_TIME FROM ACCESS_TOKEN WHERE TOKEN_VALUE = ? AND NOMBRE_USUARIO = ?";

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
            String debugMsg = String.format("Fallo al insertar token [%d] para el usuario '%s'. SQLState: %s, ErrorCode: %d",
                    tokenToInsert.getValueToken(), tokenToInsert.getUserName(), e.getSQLState(), e.getErrorCode());
            throw new DAOException(debugMsg, e);
        }
    }

    @Override
    public AuthenticationToken recoverToken(int tokenValue) throws DAOException {
        AuthenticationToken tokenRecovered = null;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT)) {

            statement.setInt(1, tokenValue);
            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    tokenRecovered = new AuthenticationToken();
                    tokenRecovered.setValueToken(resultSet.getInt("TOKEN_VALUE"));

                    Timestamp ts = resultSet.getTimestamp("CREATION_TIME");
                    if (ts != null) {
                        tokenRecovered.setTimeCreation(ts.toLocalDateTime());
                    }

                    tokenRecovered.setUserName(resultSet.getString("NOMBRE_USUARIO"));
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
                    Timestamp ts = resultSet.getTimestamp("CREATION_TIME");
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