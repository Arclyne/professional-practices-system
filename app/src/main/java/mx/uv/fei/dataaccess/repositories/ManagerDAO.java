package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IManagerDAO;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.enums.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Acceso a datos de los encargados de proyecto de las organizaciones.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class ManagerDAO extends BaseDAO implements IManagerDAO {

    private static final String SQL_INSERT_MANAGER =
            "INSERT INTO project_manager (manager_name, phone, email, status, organization_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL_MANAGERS =
            "SELECT manager_id, manager_name, phone, email, status, organization_id FROM project_manager";
    private static final String SQL_SELECT_MANAGERS_BY_ORGANIZATION =
            "SELECT manager_id, manager_name, phone, email, status, organization_id FROM project_manager WHERE organization_id = ?";
    private static final String SQL_DEACTIVATE_MANAGER =
            "UPDATE project_manager SET status = 'Inactive' WHERE manager_id = ?";
    private static final String SQL_ACTIVATE_MANAGER =
            "UPDATE project_manager SET status = 'Active' WHERE manager_id = ?";
    private static final String SQL_UPDATE_MANAGER =
            "UPDATE project_manager SET manager_name = ?, phone = ?, email = ?, organization_id = ? WHERE manager_id = ?";
    private static final String SQL_SELECT_MANAGER_BY_ID =
            "SELECT manager_id, manager_name, phone, email, status, organization_id FROM project_manager WHERE manager_id = ?";

    @Inject
    public ManagerDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<Manager> getManagersByOrganization(int organizationId) throws DAOException {
        return recoverALL(SQL_SELECT_MANAGERS_BY_ORGANIZATION, this::mapResultSetToManager, organizationId);
    }

    @Override
    public int insertManager(Manager manager) throws DAOException {
        return insertTuple(SQL_INSERT_MANAGER, statement -> {
            statement.setString(1, manager.getName());
            statement.setString(2, manager.getPhone());
            statement.setString(3, manager.getEmail());
            statement.setString(4, manager.getStatus().getDatabaseValue());
            statement.setInt(5, manager.getOrganizationId());
        });
    }

    @Override
    public List<Manager> getAllManagers() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_MANAGERS, this::mapResultSetToManager);
    }

    @Override
    public Manager recoverManager(int managerId) throws DAOException {
        List<Manager> managers = recoverALL(SQL_SELECT_MANAGER_BY_ID, this::mapResultSetToManager, managerId);
        return managers.isEmpty() ? new Manager() : managers.getFirst();
    }

    @Override
    public void updateManager(Manager manager, int managerId) throws DAOException {
        updateTuple(SQL_UPDATE_MANAGER, statement -> {
            statement.setString(1, manager.getName());
            statement.setString(2, manager.getPhone());
            statement.setString(3, manager.getEmail());
            statement.setInt(4, manager.getOrganizationId());
            statement.setInt(5, managerId);
        });
    }

    @Override
    public void activateManager(int managerId) throws DAOException {
        updateTuple(SQL_ACTIVATE_MANAGER, statement -> statement.setInt(1, managerId));
    }

    @Override
    public void deactivateMultipleManagers(List<Integer> managerIds) throws DAOException {
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                executeDeactivationBatch(connection, managerIds);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("Error al ejecutar la inactivación masiva de encargados.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Error de conexión al procesar inactivación de encargados.", e);
        }
    }

    private void executeDeactivationBatch(Connection connection, List<Integer> managerIds) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DEACTIVATE_MANAGER)) {
            for (Integer managerId : managerIds) {
                statement.setInt(1, managerId);
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            for (int result : batchResults) {
                if (result <= 0 && result != Statement.SUCCESS_NO_INFO) {
                    throw new SQLException("La inactivación masiva no afectó a uno de los encargados seleccionados.");
                }
            }
        }
    }

    private Manager mapResultSetToManager(ResultSet resultSet) throws SQLException {
        Manager manager = new Manager();
        manager.setId(resultSet.getInt("manager_id"));
        manager.setName(resultSet.getString("manager_name"));
        manager.setPhone(resultSet.getString("phone"));
        manager.setEmail(resultSet.getString("email"));
        manager.setStatus(resolveNullableStatus(resultSet));
        manager.setOrganizationId(resultSet.getInt("organization_id"));
        return manager;
    }

    private UserStatus resolveNullableStatus(ResultSet resultSet) throws SQLException {
        String statusValue = resultSet.getString("status");
        return statusValue != null ? UserStatus.fromString(statusValue) : null;
    }
}