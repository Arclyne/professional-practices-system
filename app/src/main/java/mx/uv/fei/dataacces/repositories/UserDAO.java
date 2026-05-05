package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;

@Component
public class UserDAO extends BaseDAO implements IUserDAO {

    private static final String SQL_INSERT = "INSERT INTO USUARIO (NOMBRE_USUARIO, PASSWORD, NOMBRE, APELLIDOS, CORREO, NOMBRE_ROL, ESTADO, GENERO) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_DEACTIVATE = "UPDATE USUARIO SET ESTADO = 'No Activo', FECHA_BAJA = NOW() WHERE ID_USUARIO = ?";
    private static final String SQL_UPDATE = "UPDATE USUARIO SET NOMBRE_USUARIO = ?, PASSWORD = ?, NOMBRE = ?, APELLIDOS = ?, CORREO = ?, NOMBRE_ROL = ?, ESTADO = ?, GENERO = ? WHERE ID_USUARIO = ?";

    private static final String SQL_SELECT_BY_USERNAME = "SELECT ID_USUARIO, NOMBRE_USUARIO, PASSWORD, NOMBRE, APELLIDOS, CORREO, NOMBRE_ROL, ESTADO, GENERO, FECHA_REGISTRO, FECHA_BAJA FROM USUARIO WHERE NOMBRE_USUARIO = ?";
    private static final String SQL_SELECT_BY_EMAIL = "SELECT ID_USUARIO, NOMBRE_USUARIO, PASSWORD, NOMBRE, APELLIDOS, CORREO, NOMBRE_ROL, ESTADO, GENERO, FECHA_REGISTRO, FECHA_BAJA FROM USUARIO WHERE CORREO = ?";

    private static final String SQL_VERIFY_CREDENTIALS_BY_USERNAME = "SELECT COUNT(*) FROM USUARIO WHERE NOMBRE_USUARIO = ? AND PASSWORD = ?";
    private static final String SQL_VERIFY_CREDENTIALS_BY_EMAIL = "SELECT COUNT(*) FROM USUARIO WHERE CORREO = ? AND PASSWORD = ?";

    private static final String SQL_GET_USER_ROLE = "SELECT NOMBRE_ROL FROM USUARIO WHERE NOMBRE_USUARIO = ?";

    @Inject
    public UserDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertUser(User user, Connection sharedConnection) throws DAOException {
        int generatedId = -1;

        try (PreparedStatement statement = sharedConnection.prepareStatement(SQL_INSERT,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getName());
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getEmail());
            statement.setString(6, user.getRole());
            statement.setString(7, user.getStatus());
            statement.setString(8, user.getGender());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al insertar el usuario en la transacción.", exception);
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
                statement.setString(1, user.getUserName());
                statement.setString(2, user.getPassword());
                statement.setString(3, user.getName());
                statement.setString(4, user.getLastName());
                statement.setString(5, user.getEmail());
                statement.setString(6, user.getRole());
                statement.setString(7, user.getStatus());
                statement.setString(8, user.getGender());
                statement.setInt(9, user.getId());
            });
        } catch (SQLException exception) {
            throw new DAOException("Error al actualizar el usuario en la transacción.", exception);
        }
    }

    @Override
    public boolean verifyCredentialsByUserName(String userName, String password) throws DAOException {
        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_VERIFY_CREDENTIALS_BY_USERNAME)) {
            statement.setString(1, userName);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al verificar las credenciales del usuario por nombre de usuario.", exception);
        }
        return false;
    }

    @Override
    public boolean verifyCredentialsByEmail(String email, String password) throws DAOException {
        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_VERIFY_CREDENTIALS_BY_EMAIL)) {
            statement.setString(1, email);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al verificar las credenciales del usuario por correo electrónico.", exception);
        }
        return false;
    }

    @Override
    public String getUserRole(String userName) throws DAOException {
        String retrievedRole = null;

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_GET_USER_ROLE)) {
            statement.setString(1, userName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    retrievedRole = resultSet.getString("NOMBRE_ROL");
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al recuperar el rol del usuario.", exception);
        }

        return retrievedRole;
    }

    @Override
    public User getUserByUserName(String userName) throws DAOException {
        return extractUserFromQuery(SQL_SELECT_BY_USERNAME, userName);
    }

    @Override
    public User getUserByEmail(String email) throws DAOException {
        return extractUserFromQuery(SQL_SELECT_BY_EMAIL, email);
    }

    private User extractUserFromQuery(String query, String parameter) throws DAOException {
        User retrievedUser = null;

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, parameter);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    retrievedUser = new User();
                    retrievedUser.setId(resultSet.getInt("ID_USUARIO"));
                    retrievedUser.setUserName(resultSet.getString("NOMBRE_USUARIO"));
                    retrievedUser.setPassword(resultSet.getString("PASSWORD"));
                    retrievedUser.setName(resultSet.getString("NOMBRE"));
                    retrievedUser.setLastName(resultSet.getString("APELLIDOS"));
                    retrievedUser.setEmail(resultSet.getString("CORREO"));
                    retrievedUser.setRole(resultSet.getString("NOMBRE_ROL"));
                    retrievedUser.setStatus(resultSet.getString("ESTADO"));
                    retrievedUser.setGender(resultSet.getString("GENERO"));

                    if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
                        retrievedUser.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
                    }
                    if (resultSet.getTimestamp("FECHA_BAJA") != null) {
                        retrievedUser.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al recuperar los datos del usuario de la base de datos.", exception);
        }

        return retrievedUser;
    }
}