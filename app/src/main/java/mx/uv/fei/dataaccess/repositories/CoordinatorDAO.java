package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de los coordinadores de prácticas profesionales.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class CoordinatorDAO extends BaseDAO implements ICoordinatorDAO {

    private static final String SQL_INSERT_COORDINATOR =
            "INSERT INTO coordinator (coordinator_id) VALUES (?)";
    private static final String SQL_SELECT_COORDINATOR_BY_ID =
            "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
                    "FROM coordinator c INNER JOIN user u ON c.coordinator_id = u.user_id WHERE c.coordinator_id = ?";
    private static final String SQL_SELECT_ALL_COORDINATORS =
            "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
                    "FROM coordinator c INNER JOIN user u ON c.coordinator_id = u.user_id";
    private static final String SQL_SELECT_ACTIVE_COORDINATOR =
            "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
                    "FROM coordinator c INNER JOIN user u ON c.coordinator_id = u.user_id WHERE u.status IN ('Active', 'Pending') LIMIT 1";

    private final IUserDAO userDAO;

    /**
     * Crea el DAO de coordinadores con la fuente de conexiones y el DAO de usuarios.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     * @param userDAO            DAO de usuarios usado para los datos comunes de cuenta
     */
    @Inject
    public CoordinatorDAO(IDatabaseConnection databaseConnection, IUserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    /**
     * Inserta un coordinador creando primero su usuario base dentro de una transacción.
     *
     * @param coordinator coordinador con los datos a registrar
     * @return identificador generado para el coordinador, o {@code -1} si la operación falla
     * @throws DAOException si ocurre un error y se revierte la transacción
     */
    @Override
    public int insertCoordinator(Coordinator coordinator) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int generatedUserId = userDAO.insertUser(coordinator, connection);

                if (generatedUserId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_COORDINATOR)) {
                        statement.setInt(1, generatedUserId);
                        if (statement.executeUpdate() > 0) {
                            generatedId = generatedUserId;
                        }
                    }
                }

                if (generatedId > 0) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("Error SQL al intentar insertar el coordinador. Cambios revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }

        return generatedId;
    }

    /**
     * Recupera un coordinador junto con sus datos de usuario a partir de su identificador.
     *
     * @param coordinatorId identificador del coordinador a recuperar
     * @return coordinador encontrado, o un {@link Coordinator} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public Coordinator recoverCoordinator(int coordinatorId) throws DAOException {
        Coordinator recoveredCoordinator = new Coordinator();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_COORDINATOR_BY_ID)) {

            statement.setInt(1, coordinatorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredCoordinator = mapResultSetToCoordinator(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el coordinador de la base de datos.", e);
        }

        return recoveredCoordinator;
    }

    /**
     * Recupera el coordinador en funciones, es decir el que está activo o pendiente.
     *
     * @return coordinador activo o pendiente, o {@code null} si no hay ninguno
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public Coordinator getCurrentCoordinator() throws DAOException {
        Coordinator currentCoordinator = null;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ACTIVE_COORDINATOR);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                currentCoordinator = mapResultSetToCoordinator(resultSet);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el coordinador activo de la base de datos.", e);
        }

        return currentCoordinator;
    }

    /**
     * Recupera todos los coordinadores junto con sus datos de usuario.
     *
     * @return lista con todos los coordinadores; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Coordinator> getAllCoordinators() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_COORDINATORS, this::mapResultSetToCoordinator);
    }

    /**
     * Actualiza los datos de usuario asociados a un coordinador dentro de una transacción.
     *
     * @param coordinatorToUpdate coordinador con los datos modificados
     * @param coordinatorId       identificador del coordinador a actualizar
     * @throws DAOException si ocurre un error y se revierte la transacción
     */
    @Override
    public void updateCoordinator(Coordinator coordinatorToUpdate, int coordinatorId) throws DAOException {
        coordinatorToUpdate.setId(coordinatorId);

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                userDAO.updateUser(coordinatorToUpdate, connection);
                connection.commit();
            } catch (SQLException | DAOException e) {
                connection.rollback();
                throw new DAOException("Error SQL al actualizar el coordinador. Los cambios fueron revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }
    }

    /**
     * Construye un coordinador con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return coordinador con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Coordinator mapResultSetToCoordinator(ResultSet resultSet) throws SQLException {
        Coordinator coordinator = new Coordinator();

        coordinator.setId(resultSet.getInt("user_id"));
        coordinator.setUserName(resultSet.getString("username"));
        coordinator.setPassword(resultSet.getString("password"));
        coordinator.setName(resultSet.getString("name"));
        coordinator.setLastName(resultSet.getString("last_name"));
        coordinator.setEmail(resultSet.getString("email"));
        coordinator.setRole(resultSet.getString("role_name"));
        coordinator.setStatus(resolveNullableStatus(resultSet));
        coordinator.setGender(resolveNullableGender(resultSet));
        coordinator.setRegistrationDate(resolveNullableTimestamp(resultSet, "registration_date"));
        coordinator.setDischargeDate(resolveNullableTimestamp(resultSet, "discharge_date"));

        return coordinator;
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