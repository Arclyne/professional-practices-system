package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;

@Repository
public class UserDAO implements IUserDAO {

    private final IDatabaseConnection dbConnection;

    @Autowired
    public UserDAO(IDatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public int insertUser(User user, Connection sharedConnection) throws DAOException {
        int generatedId = -1;
        String query = "INSERT INTO USUARIO (PASSWORD, NOMBRE, APELLIDOS, ESTADO, GENERO) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement statement = sharedConnection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getPassword());
            statement.setString(2, user.getName());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getStatus());
            statement.setString(5, user.getGender());

            if (statement.executeUpdate() > 0) {
                try (java.sql.ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al insertar el usuario en la transacción.", e);
        }

        return generatedId;
    }

    @Override
    public boolean deactivateUser(int idUsuario) throws DAOException {
        String query = "UPDATE USUARIO SET ESTADO = 'no activo', FECHA_BAJA = NOW() WHERE ID_USUARIO = ?";
        boolean isDeactivated = false;

        try (
                Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, idUsuario);
            isDeactivated = (statement.executeUpdate() > 0);

        } catch (SQLException e) {
            throw new DAOException("Error al desactivar el usuario con ID: " + idUsuario, e);
        }

        return isDeactivated;
    }
}