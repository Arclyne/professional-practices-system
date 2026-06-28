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

    /**
     * Crea el DAO de usuarios con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public UserDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta un usuario dentro de una transacción gobernada por una conexión compartida.
     *
     * @param user             usuario con los datos a registrar
     * @param sharedConnection conexión compartida que controla la transacción en curso
     * @return identificador generado para el nuevo usuario, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al insertar el usuario
     */
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

    /**
     * Marca a un usuario como inactivo y registra su fecha de baja.
     *
     * @param userId identificador del usuario a inactivar
     * @throws DAOException si el usuario no existe o si ocurre un error al actualizar
     */
    @Override
    public void deactivateUser(int userId) throws DAOException {
        updateTuple(SQL_DEACTIVATE_USER, statement -> {
            statement.setInt(1, userId);
        });
    }

    /**
     * Reactiva a un usuario y limpia su fecha de baja.
     *
     * @param userId identificador del usuario a reactivar
     * @throws DAOException si el usuario no existe o si ocurre un error al actualizar
     */
    @Override
    public void activateUser(int userId) throws DAOException {
        updateTuple(SQL_ACTIVATE_USER, statement -> {
            statement.setInt(1, userId);
        });
    }

    /**
     * Actualiza los datos de un usuario dentro de una transacción compartida.
     *
     * @param user             usuario con los datos modificados, incluido su identificador
     * @param sharedConnection conexión compartida que controla la transacción en curso
     * @throws DAOException si el usuario no existe o si ocurre un error al actualizar
     */
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

    /**
     * Recupera el nombre del rol asignado a un usuario a partir de su nombre de usuario.
     *
     * @param userName nombre de usuario por el cual buscar
     * @return nombre del rol, o {@code null} si el usuario no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Recupera un usuario a partir de su nombre de usuario.
     *
     * @param userName nombre de usuario por el cual buscar
     * @return usuario encontrado, o un {@link User} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public User getUserByUserName(String userName) throws DAOException {
        return extractUserFromQuery(SQL_SELECT_USER_BY_USERNAME, userName);
    }

    /**
     * Recupera un usuario a partir de su correo electrónico.
     *
     * @param email correo electrónico por el cual buscar
     * @return usuario encontrado, o un {@link User} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public User getUserByEmail(String email) throws DAOException {
        return extractUserFromQuery(SQL_SELECT_USER_BY_EMAIL, email);
    }

    /**
     * Inactiva varios usuarios en una sola transacción por lotes, con reversión ante errores.
     *
     * @param userIds identificadores de los usuarios a inactivar
     * @throws DAOException si la operación por lotes falla o si ocurre un error de conexión
     */
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

    /**
     * Ejecuta por lotes la inactivación de los usuarios indicados sobre la conexión recibida.
     *
     * @param connection conexión transaccional sobre la que se ejecuta el lote
     * @param userIds    identificadores de los usuarios a inactivar
     * @throws SQLException si el lote no afecta a alguno de los usuarios o si ocurre un error
     */
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

    /**
     * Ejecuta una consulta de un solo parámetro y construye el usuario a partir del resultado.
     *
     * @param sqlStatement sentencia SQL de selección a ejecutar
     * @param parameter    valor a enlazar como único parámetro de la consulta
     * @return usuario mapeado, o un {@link User} vacío si no hay resultados
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Construye un usuario con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return usuario con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
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

    /**
     * Obtiene el estado del usuario tolerando valores nulos en la columna.
     *
     * @param resultSet resultado posicionado en la fila a leer
     * @return estado del usuario, o {@code null} si la columna es nula
     * @throws SQLException si ocurre un error al leer la columna
     */
    private UserStatus resolveNullableStatus(ResultSet resultSet) throws SQLException {
        String statusValue = resultSet.getString("status");
        return statusValue != null ? UserStatus.fromString(statusValue) : null;
    }

    /**
     * Obtiene el género del usuario tolerando valores nulos en la columna.
     *
     * @param resultSet resultado posicionado en la fila a leer
     * @return género del usuario, o {@code null} si la columna es nula
     * @throws SQLException si ocurre un error al leer la columna
     */
    private Gender resolveNullableGender(ResultSet resultSet) throws SQLException {
        String genderValue = resultSet.getString("gender");
        return genderValue != null ? Gender.fromDatabaseValue(genderValue) : null;
    }

    /**
     * Obtiene una marca de tiempo como {@link LocalDateTime} tolerando valores nulos.
     *
     * @param resultSet  resultado posicionado en la fila a leer
     * @param columnName nombre de la columna de tipo marca de tiempo
     * @return fecha y hora correspondiente, o {@code null} si la columna es nula
     * @throws SQLException si ocurre un error al leer la columna
     */
    private LocalDateTime resolveNullableTimestamp(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}