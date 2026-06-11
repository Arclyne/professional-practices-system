
package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IAuthenticationToken;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.AuthenticationToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Component
public class AuthenticationTokenDAO extends BaseDAO implements IAuthenticationToken {

    private static final String SQL_INSERT =
            "INSERT INTO access_token (token_value, creation_time, username) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_BY_VALUE =
            "SELECT token_value, creation_time, username FROM access_token WHERE token_value = ?";
    private static final String SQL_SELECT_CREATION_TIME_BY_VALUE_AND_USER =
            "SELECT creation_time FROM access_token WHERE token_value = ? AND username = ?";

    @Inject
    public AuthenticationTokenDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public boolean insertToken(AuthenticationToken tokenToInsert) throws DAOException {
        boolean isInserted = false;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {

            statement.setInt(1, tokenToInsert.getValueToken());
            statement.setTimestamp(2, Timestamp.valueOf(tokenToInsert.getTimeCreation()));
            statement.setString(3, tokenToInsert.getUserName());

            isInserted = statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException(buildTokenDebugMessage(
                    "Fallo al insertar token [%d] para el usuario '%s'",
                    tokenToInsert.getValueToken(), tokenToInsert.getUserName(), e), e);
        }

        return isInserted;
    }

    @Override
    public AuthenticationToken recoverToken(int tokenValue) throws DAOException {
        AuthenticationToken recoveredToken = new AuthenticationToken();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_VALUE)) {

            statement.setInt(1, tokenValue);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredToken = mapResultSetToAuthenticationToken(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException(buildTokenDebugMessage(
                    "Fallo al recuperar los datos del token [%d] para el usuario '%s'",
                    tokenValue, null, e), e);
        }

        return recoveredToken;
    }

    @Override
    public LocalDateTime getTokenCreationTime(int tokenValue, String userName) throws DAOException {
        LocalDateTime creationTime = null;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_CREATION_TIME_BY_VALUE_AND_USER)) {

            statement.setInt(1, tokenValue);
            statement.setString(2, userName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    creationTime = resolveNullableTimestamp(resultSet, "creation_time");
                }
            }
        } catch (SQLException e) {
            throw new DAOException(buildTokenDebugMessage(
                    "Fallo al consultar tiempo de creación para el token [%d] del usuario '%s'",
                    tokenValue, userName, e), e);
        }

        return creationTime;
    }

    private AuthenticationToken mapResultSetToAuthenticationToken(ResultSet resultSet) throws SQLException {
        AuthenticationToken token = new AuthenticationToken();
        token.setValueToken(resultSet.getInt("token_value"));
        token.setTimeCreation(resolveNullableTimestamp(resultSet, "creation_time"));
        token.setUserName(resultSet.getString("username"));
        return token;
    }

    private LocalDateTime resolveNullableTimestamp(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }

    private String buildTokenDebugMessage(String template, int tokenValue, String userName, SQLException e) {
        return String.format(template + ". SQLState: %s, ErrorCode: %d",
                tokenValue, userName != null ? userName : "N/A", e.getSQLState(), e.getErrorCode());
    }
}