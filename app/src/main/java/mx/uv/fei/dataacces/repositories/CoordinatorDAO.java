package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class CoordinatorDAO extends BaseDAO implements ICoordinatorDAO {

    private final UserDAO userDAO;
    private static final String SQL_INSERT = "INSERT INTO coordinator (coordinator_id) VALUES (?)";

    private static final String SQL_SELECT_ONE = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
            "FROM coordinator c INNER JOIN user u ON c.coordinator_id = u.user_id WHERE c.coordinator_id = ?";
    private static final String SQL_SELECT_ALL = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
            "FROM coordinator c INNER JOIN user u ON c.coordinator_id = u.user_id";

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

        try (
                Connection connection = databaseConnection.getConnection();
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
    }
}