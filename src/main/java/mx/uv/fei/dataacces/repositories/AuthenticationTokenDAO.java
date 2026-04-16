package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.uv.fei.domain.dto.AuthenticationToken;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IAuthenticationToken;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

@Repository
public class AuthenticationTokenDAO implements IAuthenticationToken {
    private final IDatabaseConnection dbConnection;
    private static final String SQL_INSERT = "INSERT INTO ACCESS_TOKEN (TOKEN_VALUE, CREATION_TIME, ID_USUARIO) VALUES (?, ?, ?)";
    private static final String SQL_SELECT = "SELECT TOKEN_VALUE, CREATION_TIME, ID_USUARIO FROM ACCESS_TOKEN WHERE TOKEN_VALUE = ?";

    @Autowired
    public AuthenticationTokenDAO(IDatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public boolean insertToken(AuthenticationToken tokenToInsert) throws DAOException {
        try (
                Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {

            statement.setInt(1, tokenToInsert.getValueToken());
            statement.setObject(2, tokenToInsert.getTimeCreation());
            statement.setInt(3, tokenToInsert.getUserId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar el token en la base de datos.", e);
        }
    }

    public AuthenticationToken recoverToken(int tokenValue) throws DAOException {

        AuthenticationToken tokenRecovered = null;

        try (
                Connection connection = dbConnection.getConnection();
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
            throw new DAOException("Error al intentar insertar el token en la base de datos.", e);
        }
        return tokenRecovered;
    }
}
