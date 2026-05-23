package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class CoordinatorDAO extends BaseDAO implements ICoordinatorDAO {

    private static final String SQL_INSERT_COORDINATOR = "INSERT INTO coordinator (coordinator_id) VALUES (?)";
    private static final String SQL_SELECT_COORDINATOR_BY_ID = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
            "FROM coordinator c INNER JOIN user u ON c.coordinator_id = u.user_id WHERE c.coordinator_id = ?";
    private static final String SQL_SELECT_ALL_COORDINATORS = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
            "FROM coordinator c INNER JOIN user u ON c.coordinator_id = u.user_id";
    private static final String SQL_SELECT_ACTIVE_COORDINATOR = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
            "FROM coordinator c INNER JOIN user u ON c.coordinator_id = u.user_id WHERE u.status IN ('Active', 'Pending') LIMIT 1";

    private final IUserDAO userDAO;

    @Inject
    public CoordinatorDAO(IDatabaseConnection databaseConnection, IUserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertCoordinator(Coordinator coordinator) throws DAOException {
        int resultId = -1;

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                resultId = executeCoordinatorTransaction(connection, coordinator);

                if (resultId > 0) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException exception) {
                connection.rollback();
                throw new DAOException("Error SQL al intentar insertar el coordinador. Cambios revertidos.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new DAOException("Error crítico de conexión a la base de datos.", exception);
        }

        return resultId;
    }

    private int executeCoordinatorTransaction(Connection connection, Coordinator coordinator) throws SQLException, DAOException {
        int insertedCoordinatorId = -1;
        int generatedUserId = userDAO.insertUser(coordinator, connection);

        if (generatedUserId > 0) {
            try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_COORDINATOR)) {
                statement.setInt(1, generatedUserId);

                if (statement.executeUpdate() > 0) {
                    insertedCoordinatorId = generatedUserId;
                }
            }
        }

        return insertedCoordinatorId;
    }

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
        } catch (SQLException exception) {
            throw new DAOException("Error al intentar recuperar el coordinador de la base de datos.", exception);
        }

        return recoveredCoordinator;
    }

    @Override
    public Coordinator getCurrentCoordinator() throws DAOException {
        Coordinator currentCoordinator = null;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ACTIVE_COORDINATOR);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                currentCoordinator = mapResultSetToCoordinator(resultSet);
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al intentar recuperar el coordinador actual activo de la base de datos.", exception);
        }

        return currentCoordinator;
    }

    @Override
    public List<Coordinator> getAllCoordinators() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_COORDINATORS, this::mapResultSetToCoordinator);
    }

    @Override
    public boolean updateCoordinator(Coordinator coordinatorToUpdate, int coordinatorId) throws DAOException {
        boolean isUpdated = false;
        coordinatorToUpdate.setId(coordinatorId);

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                isUpdated = userDAO.updateUser(coordinatorToUpdate, connection);

                if (isUpdated) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("Error SQL al actualizar el coordinador. Los cambios fueron revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }

        return isUpdated;
    }

    private Coordinator mapResultSetToCoordinator(ResultSet resultSet) throws SQLException {
        Coordinator coordinator = new Coordinator();

        coordinator.setId(resultSet.getInt("user_id"));
        coordinator.setUserName(resultSet.getString("username"));
        coordinator.setPassword(resultSet.getString("password"));
        coordinator.setName(resultSet.getString("name"));
        coordinator.setLastName(resultSet.getString("last_name"));
        coordinator.setEmail(resultSet.getString("email"));
        coordinator.setRole(resultSet.getString("role_name"));

        String statusValue = resultSet.getString("status");
        coordinator.setStatus(statusValue != null ? UserStatus.fromString(statusValue) : null);

        String genderValue = resultSet.getString("gender");
        coordinator.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

        if (resultSet.getTimestamp("registration_date") != null) {
            coordinator.setRegistrationDate(resultSet.getTimestamp("registration_date").toLocalDateTime());
        }
        if (resultSet.getTimestamp("discharge_date") != null) {
            coordinator.setDischargeDate(resultSet.getTimestamp("discharge_date").toLocalDateTime());
        }

        return coordinator;
    }
}