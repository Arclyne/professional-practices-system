package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class UserDAO extends BaseDAO implements IUserDAO {


    private static final String SQL_INSERT = "INSERT INTO user (USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, ROLE_NAME, STATUS, GENDER) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_DEACTIVATE = "UPDATE user SET STATUS = 'Inactive', TERMINATION_DATE = NOW() WHERE ID_USER = ?";
    private static final String SQL_UPDATE = "UPDATE user SET USERNAME = ?, PASSWORD = ?, FIRST_NAME = ?, LAST_NAME = ?, EMAIL = ?, ROLE_NAME = ?, STATUS = ?, GENDER = ? WHERE ID_USER = ?";

    private static final String SQL_SELECT_BY_USERNAME = "SELECT ID_USER, USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, ROLE_NAME, STATUS, GENDER, REGISTRATION_DATE, TERMINATION_DATE FROM user WHERE USERNAME = ?";
    private static final String SQL_SELECT_BY_EMAIL = "SELECT ID_USER, USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL, ROLE_NAME, STATUS, GENDER, REGISTRATION_DATE, TERMINATION_DATE FROM user WHERE EMAIL = ?";

    private static final String SQL_VERIFY_CREDENTIALS_BY_USERNAME = "SELECT COUNT(*) FROM user WHERE USERNAME = ? AND PASSWORD = ?";
    private static final String SQL_VERIFY_CREDENTIALS_BY_EMAIL = "SELECT COUNT(*) FROM user WHERE EMAIL = ? AND PASSWORD = ?";

    private static final String SQL_GET_USER_ROLE = "SELECT ROLE_NAME FROM user WHERE USERNAME = ?";

    @Inject
    public UserDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertUser(User user, Connection sharedConnection) throws DAOException {
        int generatedId = -1;

        try (PreparedStatement statement = sharedConnection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getName()); // Se mapea a FIRST_NAME
            statement.setString(4, user.getLastName());
            statement.setString(5, user.getEmail());
            statement.setString(6, user.getRole());
            statement.setString(7, user.getStatus() != null ? user.getStatus().getDatabaseValue() : null);
            statement.setString(8, user.getGender() != null ? user.getGender().getDatabaseValue() : null);

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
                statement.setString(7, user.getStatus() != null ? user.getStatus().getDatabaseValue() : null);
                statement.setString(8, user.getGender() != null ? user.getGender().getDatabaseValue() : null);
                statement.setInt(9, user.getId());
            });
        } catch (SQLException exception) {
            throw new DAOException("Error al actualizar el usuario en la transacción.", exception);
        }
    }

    @Override
    public boolean verifyCredentialsByUserName(String userName, String password) throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
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
        try (Connection connection = databaseConnection.getConnection();
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

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_GET_USER_ROLE)) {

            statement.setString(1, userName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    retrievedRole = resultSet.getString("ROLE_NAME"); // Actualizado a mayúsculas
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

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, parameter);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    retrievedUser = new User();

                    retrievedUser.setId(resultSet.getInt("ID_USER"));
                    retrievedUser.setUserName(resultSet.getString("USERNAME"));
                    retrievedUser.setPassword(resultSet.getString("PASSWORD"));
                    retrievedUser.setName(resultSet.getString("FIRST_NAME"));
                    retrievedUser.setLastName(resultSet.getString("LAST_NAME"));
                    retrievedUser.setEmail(resultSet.getString("EMAIL"));
                    retrievedUser.setRole(resultSet.getString("ROLE_NAME"));

                    String statusValue = resultSet.getString("STATUS");
                    retrievedUser.setStatus(statusValue != null ? UserStatus.fromString(statusValue) : null);

                    String genderValue = resultSet.getString("GENDER");
                    retrievedUser.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

                    if (resultSet.getTimestamp("REGISTRATION_DATE") != null) {
                        retrievedUser.setRegistrationDate(resultSet.getTimestamp("REGISTRATION_DATE").toLocalDateTime());
                    }
                    if (resultSet.getTimestamp("TERMINATION_DATE") != null) {
                        retrievedUser.setDischargeDate(resultSet.getTimestamp("TERMINATION_DATE").toLocalDateTime());
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al recuperar los datos del usuario de la base de datos.", exception);
        }

        return retrievedUser;
    }

    @Override
    public boolean deactivateMultipleUsers(List<Integer> userIdentifiersList) throws DAOException {
        boolean allUpdatesSuccessful = true;
        try (Connection activeDatabaseConnection = databaseConnection.getConnection()) {
            activeDatabaseConnection.setAutoCommit(false);

            try (PreparedStatement updateStatement = activeDatabaseConnection.prepareStatement(SQL_DEACTIVATE)) {
                for (Integer currentIdentifier : userIdentifiersList) {
                    updateStatement.setInt(1, currentIdentifier);
                    updateStatement.addBatch();
                }
                int[] executionResults = updateStatement.executeBatch();
                for (int result : executionResults) {
                    if (result <= 0) {
                        allUpdatesSuccessful = false;
                        break;
                    }
                }
                if (allUpdatesSuccessful) {
                    activeDatabaseConnection.commit();
                } else {
                    activeDatabaseConnection.rollback();
                }
            } catch (SQLException executionException) {
                activeDatabaseConnection.rollback();
                throw new DAOException("Error al ejecutar la inactivación masiva de usuarios.", executionException);
            } finally {
                activeDatabaseConnection.setAutoCommit(true);
            }
        } catch (SQLException connectionException) {
            throw new DAOException("Error de conexión al procesar inactivación masiva.", connectionException);
        }
        return allUpdatesSuccessful;
    }
}