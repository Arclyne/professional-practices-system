package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;

public class UserDAO extends BaseDAO implements IUserDAO {
    private static final String SQL_INSERT = "INSERT INTO USUARIO (PASSWORD, NOMBRE, APELLIDOS, ESTADO, GENERO) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_DEACTIVATE = "UPDATE USUARIO SET ESTADO = 'no activo', FECHA_BAJA = NOW() WHERE ID_USUARIO = ?";
    private static final String SQL_UPDATE = "UPDATE USUARIO SET PASSWORD = ?, NOMBRE = ?, APELLIDOS = ?, ESTADO = ?, GENERO = ? WHERE ID_USUARIO = ?";

    public UserDAO(IDatabaseConnection dbConnection) {
        super(dbConnection);
    }

    @Override
    public int insertUser(User user, Connection sharedConnection) throws DAOException {
        int generatedId = -1;

        try (PreparedStatement statement = sharedConnection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS)) {
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
        return updateTuple(SQL_DEACTIVATE, statement -> {
            statement.setInt(1, idUsuario);
        });
    }

    @Override
    public boolean updateUser(User user, Connection sharedConnection) throws DAOException {
        try {
            return updateTuple(sharedConnection, SQL_UPDATE, statement -> {
                statement.setString(1, user.getPassword());
                statement.setString(2, user.getName());
                statement.setString(3, user.getLastName());
                statement.setString(4, user.getStatus());
                statement.setString(5, user.getGender());
                statement.setInt(6, user.getId());
            });
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el usuario en la transacción.", e);
        }
    }
}