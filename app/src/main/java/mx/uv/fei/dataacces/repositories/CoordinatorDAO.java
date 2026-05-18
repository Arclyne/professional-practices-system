package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class CoordinatorDAO extends BaseDAO implements ICoordinatorDAO {

    private final UserDAO userDAO;
    private static final String SQL_INSERT = "INSERT INTO coordinator (ID_COORDINADOR) VALUES (?)";

    private static final String SQL_SELECT_ONE = "SELECT u.ID_USER, u.USERNAME, u.PASSWORD, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.ROLE_NAME, u.STATUS, u.GENDER, u.REGISTRATION_DATE, u.TERMINATION_DATE " +
            "FROM coordinator c INNER JOIN user u ON c.ID_COORDINADOR = u.ID_USER WHERE c.ID_COORDINADOR = ?";

    private static final String SQL_SELECT_ALL = "SELECT u.ID_USER, u.USERNAME, u.PASSWORD, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.ROLE_NAME, u.STATUS, u.GENDER, u.REGISTRATION_DATE, u.TERMINATION_DATE " +
            "FROM coordinator c INNER JOIN user u ON c.ID_COORDINADOR = u.ID_USER";

    private static final String SQL_SELECT_CURRENT_ACTIVE = "SELECT u.ID_USER, u.USERNAME, u.PASSWORD, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.ROLE_NAME, u.STATUS, u.GENDER, u.REGISTRATION_DATE, u.TERMINATION_DATE " +
            "FROM coordinator c INNER JOIN user u ON c.ID_COORDINADOR = u.ID_USER WHERE u.STATUS = 'Active' LIMIT 1";

    @Inject
    public CoordinatorDAO(IDatabaseConnection databaseConnection, UserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertCoordinator(Coordinator coordinator) throws DAOException {
        int resultId = -1;
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int generatedUserId = userDAO.insertUser(coordinator, connection);
                if (generatedUserId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
                        statement.setInt(1, generatedUserId);
                        if (statement.executeUpdate() > 0) {
                            resultId = generatedUserId;
                        }
                    }
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException exception) {
                connection.rollback();
                throw new DAOException("SQL Error al intentar insertar el coordinador. Cambios revertidos.", exception);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException exception) {
            throw new DAOException("Error crítico de conexión a la base de datos.", exception);
        }
        return resultId;
    }

    @Override
    public Coordinator recoverCoordinator(int coordinatorId) throws DAOException {
        Coordinator coordinatorToSearch = new Coordinator();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {
            statement.setInt(1, coordinatorId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    mapCoordinator(coordinatorToSearch, resultSet);
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al intentar recuperar el coordinador de la base de datos.", exception);
        }
        return coordinatorToSearch;
    }

    @Override
    public Coordinator getCurrentCoordinator() throws DAOException {
        Coordinator currentCoordinator = null;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_CURRENT_ACTIVE);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                currentCoordinator = new Coordinator();
                mapCoordinator(currentCoordinator, resultSet);
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al intentar recuperar el coordinador actual activo de la base de datos.", exception);
        }
        return currentCoordinator;
    }

    @Override
    public List<Coordinator> getAllCoordinators() throws DAOException {
        return recoverALL(SQL_SELECT_ALL, resultSet -> {
            Coordinator coordinatorRecovered = new Coordinator();
            mapCoordinator(coordinatorRecovered, resultSet);
            return coordinatorRecovered;
        });
    }

    @Override
    public boolean updateCoordinator(Coordinator coordinatorToUpdate, int id) throws DAOException {
        boolean isUpdated = false;
        coordinatorToUpdate.setId(id);
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
                throw new DAOException("SQL Error while updating coordinator. Changes were rolled back.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Critical database connection error.", e);
        }
        return isUpdated;
    }

    private void mapCoordinator(Coordinator coordinator, ResultSet resultSet) throws SQLException {
        coordinator.setId(resultSet.getInt("ID_USER"));
        coordinator.setUserName(resultSet.getString("USERNAME"));
        coordinator.setPassword(resultSet.getString("PASSWORD"));
        coordinator.setName(resultSet.getString("FIRST_NAME"));
        coordinator.setLastName(resultSet.getString("LAST_NAME"));
        coordinator.setEmail(resultSet.getString("EMAIL"));
        coordinator.setRole(resultSet.getString("ROLE_NAME"));

        String statusValue = resultSet.getString("STATUS");
        coordinator.setStatus(statusValue != null ? UserStatus.fromString(statusValue) : null);

        String genderValue = resultSet.getString("GENDER");
        coordinator.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

        if (resultSet.getTimestamp("REGISTRATION_DATE") != null) {
            coordinator.setRegistrationDate(resultSet.getTimestamp("REGISTRATION_DATE").toLocalDateTime());
        }
        if (resultSet.getTimestamp("TERMINATION_DATE") != null) {
            coordinator.setDischargeDate(resultSet.getTimestamp("TERMINATION_DATE").toLocalDateTime());
        }
    }
}