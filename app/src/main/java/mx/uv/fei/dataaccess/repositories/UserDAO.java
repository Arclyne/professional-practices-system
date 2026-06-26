package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de los usuarios y la verificación de sus credenciales.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class UserDAO extends BaseDAO implements IUserDAO {

    private static final String SQL_INSERT_USER =
            "INSERT INTO user (username, password, name, last_name, email, role_name, status, gender) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_USER =
            "UPDATE user SET username = ?, password = ?, name = ?, last_name = ?, email = ?, role_name = ?, status = ?, gender = ? WHERE user_id = ?";
    private static final String SQL_DEACTIVATE_USER =
            "UPDATE user SET status = 'Inactive', discharge_date = NOW() WHERE user_id = ?";
    private static final String SQL_ACTIVATE_USER =
            "UPDATE user SET status = 'Active', discharge_date = NULL WHERE user_id = ?";
    private static final String SQL_SELECT_USER_BY_USERNAME =
            "SELECT user_id, username, password, name, last_name, email, role_name, status, gender, registration_date, discharge_date FROM user WHERE username = ?";
    private static final String SQL_SELECT_USER_BY_EMAIL =
            "SELECT user_id, username, password, name, last_name, email, role_name, status, gender, registration_date, discharge_date FROM user WHERE email = ?";
    private static final String SQL_SELECT_USER_ROLE_BY_USERNAME =
            "SELECT role_name FROM user WHERE username = ?";

    @Inject
    public UserDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertUser(User user, Connection sharedConnection) throws DAOException {
        int generatedId = -1;

        try (PreparedStatement statement = sharedConnection.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, user.getUserName());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getName());
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
        } catch (SQLException e) {
            throw new DAOException("Error al insertar el usuario en la transacción.", e);
        }

        return generatedId;
    }

    @Override
    public void deactivateUser(int userId) throws DAOException {
        updateTuple(SQL_DEACTIVATE_USER, statement -> {
            statement.setInt(1, userId);
        });
    }

    @Override
    public void activateUser(int userId) throws DAOException {
        updateTuple(SQL_ACTIVATE_USER, statement -> {
            statement.setInt(1, userId);
        });
    }

    @Override
    public void updateUser(User user, Connection sharedConnection) throws DAOException {
        try {
            updateTuple(sharedConnection, SQL_UPDATE_USER, statement -> {
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
        } catch (SQLException e) {
            throw new DAOException("Error al actualizar el usuario en la transacción.", e);
        }
    }

    @Override
    public String getUserRole(String userName) throws DAOException {
        String retrievedRole = null;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_USER_ROLE_BY_USERNAME)) {

            statement.setString(1, userName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    retrievedRole = resultSet.getString("role_name");
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar el rol del usuario.", e);
        }

        return retrievedRole;
    }

    @Override
    public User getUserByUserName(String userName) throws DAOException {
        return extractUserFromQuery(SQL_SELECT_USER_BY_USERNAME, userName);
    }

    @Override
    public User getUserByEmail(String email) throws DAOException {
        return extractUserFromQuery(SQL_SELECT_USER_BY_EMAIL, email);
    }

    @Override
    public void deactivateMultipleUsers(List<Integer> userIds) throws DAOException {
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                executeDeactivationBatch(connection, userIds);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("Error al ejecutar la inactivación masiva de usuarios.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error de conexión al procesar inactivación masiva.", e);
        }
    }

    private void executeDeactivationBatch(Connection connection, List<Integer> userIds) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DEACTIVATE_USER)) {
            for (Integer userId : userIds) {
                statement.setInt(1, userId);
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            for (int result : batchResults) {
                if (result <= 0 && result != Statement.SUCCESS_NO_INFO) {
                    throw new SQLException("La inactivación masiva no afectó a uno de los usuarios seleccionados.");
                }
            }
        }
    }

    private User extractUserFromQuery(String sqlStatement, String parameter) throws DAOException {
        User retrievedUser = new User();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sqlStatement)) {

            statement.setString(1, parameter);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    retrievedUser = mapResultSetToUser(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar los datos del usuario de la base de datos.", e);
        }

        return retrievedUser;
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setUserName(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setName(resultSet.getString("name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));
        user.setRole(resultSet.getString("role_name"));
        user.setStatus(resolveNullableStatus(resultSet));
        user.setGender(resolveNullableGender(resultSet));
        user.setRegistrationDate(resolveNullableTimestamp(resultSet, "registration_date"));
        user.setDischargeDate(resolveNullableTimestamp(resultSet, "discharge_date"));
        return user;
    }

    private UserStatus resolveNullableStatus(ResultSet resultSet) throws SQLException {
        String statusValue = resultSet.getString("status");
        return statusValue != null ? UserStatus.fromString(statusValue) : null;
    }

    private Gender resolveNullableGender(ResultSet resultSet) throws SQLException {
        String genderValue = resultSet.getString("gender");
        return genderValue != null ? Gender.fromDatabaseValue(genderValue) : null;
    }

    private LocalDateTime resolveNullableTimestamp(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}