package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import mx.uv.fei.domain.dto.AuthenticationToken;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IAuthenticationToken;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

public class AuthenticationTokenDAO extends BaseDAO implements IAuthenticationToken {

    private static final String SQL_INSERT = "INSERT INTO ACCESS_TOKEN (TOKEN_VALUE, CREATION_TIME, ID_USUARIO) VALUES (?, ?, ?)";
    private static final String SQL_SELECT = "SELECT TOKEN_VALUE, CREATION_TIME, ID_USUARIO FROM ACCESS_TOKEN WHERE TOKEN_VALUE = ?";
    private static final String SQL_SELECT_CREATION_TIME = "SELECT CREATION_TIME FROM ACCESS_TOKEN WHERE TOKEN_VALUE = ? AND ID_USUARIO = ?";

    public AuthenticationTokenDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public boolean insertToken(AuthenticationToken tokenToInsert) throws DAOException {
        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {

            statement.setInt(1, tokenToInsert.getValueToken());
            statement.setObject(2, tokenToInsert.getTimeCreation());
            statement.setInt(3, tokenToInsert.getUserId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar el token en la base de datos.", e);
        }
    }

    @Override
    public AuthenticationToken recoverToken(int tokenValue) throws DAOException {
        AuthenticationToken tokenRecovered = null;

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT)) {

            statement.setInt(1, tokenValue);
            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    tokenRecovered = new AuthenticationToken();
                    tokenRecovered.setValueToken(resultSet.getInt("TOKEN_VALUE"));
                    tokenRecovered.setTimeCreation(resultSet.getObject("CREATION_TIME", LocalDateTime.class));
                    tokenRecovered.setUserId(resultSet.getInt("ID_USUARIO"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el token de la base de datos.", e);
        }
        return tokenRecovered;
    }

    @Override
    public LocalDateTime getTokenCreationTime(int tokenValue, int userId) throws DAOException {
        LocalDateTime creationTime = null;

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_CREATION_TIME)) {

            statement.setInt(1, tokenValue);
            statement.setInt(2, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    creationTime = resultSet.getObject("CREATION_TIME", LocalDateTime.class);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar la fecha de creación del token.", e);
        }
        return creationTime;
    }
}