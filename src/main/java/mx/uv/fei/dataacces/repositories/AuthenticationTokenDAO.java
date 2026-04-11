package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.domain.dto.AuthenticationToken;
import mx.uv.fei.dataacces.exceptions.DAOException;

public class AuthenticationTokenDAO {
    private static final String SQL_INSERT = "INSERT INTO ACCESS_TOKEN (TOKEN_VALUE, CREATION_TIME, ID_USUARIO ) VALUES (?, ?, ?)";
    private static final String SQL_SELECT = "SELECT TOKEN_VALUE, CREATION_TIME, ID_USUARIO FROM ACCESS_TOKEN WHERE TOKEN_VALUE = ?";

    public boolean insertToken(AuthenticationToken token) throws DAOException {
        try (
                Connection connection = DatabaseConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {

            statement.setInt(1, token.getValueToken());
            statement.setObject(2, token.getTimeCreation());
            statement.setInt(3, token.getUserId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar el token en la base de datos.", e);
        }
    }

    public AuthenticationToken recoverToken(int tokenValue) throws DAOException {

        AuthenticationToken token = null;

        try (
                Connection connection = DatabaseConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT)) {

            statement.setInt(1, tokenValue);
            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    token = new AuthenticationToken();

                    token.setValueToken(resultSet.getInt("TOKEN_VALUE"));

                    token.setTimeCreation(resultSet.getObject("CREATION_TIME", LocalDateTime.class));

                    token.setUserId(resultSet.getInt("ID_USUARIO"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar el token en la base de datos.", e);
        }
        return token;
    }
}
