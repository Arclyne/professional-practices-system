package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IManagerDAO;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class ManagerDAO extends BaseDAO implements IManagerDAO {

    private static final String SQL_SELECT_BY_ORG = "SELECT ID_MANAGER, MANAGER_NAME, PHONE, EMAIL, STATUS FROM project_manager WHERE ID_ORGANIZATION = ?";
    private static final String SQL_INSERT_MANAGER = "INSERT INTO project_manager (MANAGER_NAME, PHONE, EMAIL, ID_ORGANIZATION, STATUS) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL_MANAGERS = "SELECT ID_MANAGER, MANAGER_NAME, PHONE, EMAIL, ID_ORGANIZATION, STATUS FROM project_manager";
    private static final String SQL_DEACTIVATE_MANAGER = "UPDATE project_manager SET STATUS = ? WHERE ID_MANAGER = ?";

    @Inject
    public ManagerDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<Manager> getManagersByOrganization(int organizationId) throws DAOException {
        List<Manager> managersList = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ORG)) {
            statement.setInt(1, organizationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Manager manager = new Manager();
                    manager.setId(resultSet.getInt("ID_MANAGER"));
                    manager.setName(resultSet.getString("MANAGER_NAME"));
                    manager.setPhone(resultSet.getString("PHONE"));
                    manager.setEmail(resultSet.getString("EMAIL"));
                    manager.setOrganizationId(organizationId);
                    manager.setStatus(UserStatus.fromString(resultSet.getString("STATUS")));
                    managersList.add(manager);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar los encargados por organización en la base de datos.", e);
        }
        return managersList;
    }

    @Override
    public boolean insertManager(Manager manager) throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_MANAGER)) {
            statement.setString(1, manager.getName());
            statement.setString(2, manager.getPhone());
            statement.setString(3, manager.getEmail());
            statement.setInt(4, manager.getOrganizationId());
            statement.setString(5, manager.getStatus().getDatabaseValue());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al intentar registrar el encargado en la base de datos.", e);
        }
    }

    @Override
    public List<Manager> getAllManagers() throws DAOException {
        List<Manager> managersList = new ArrayList<>();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL_MANAGERS);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Manager manager = new Manager();
                manager.setId(resultSet.getInt("ID_MANAGER"));
                manager.setName(resultSet.getString("MANAGER_NAME"));
                manager.setPhone(resultSet.getString("PHONE"));
                manager.setEmail(resultSet.getString("EMAIL"));
                manager.setOrganizationId(resultSet.getInt("ID_ORGANIZATION"));
                manager.setStatus(UserStatus.fromString(resultSet.getString("STATUS")));
                managersList.add(manager);
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar todos los encargados en la base de datos.", e);
        }
        return managersList;
    }

    @Override
    public boolean deactivateMultipleManagers(List<Integer> managerIdentifiersList) throws DAOException {
        boolean allUpdatesSuccessful = true;
        try (Connection activeDatabaseConnection = databaseConnection.getConnection()) {
            activeDatabaseConnection.setAutoCommit(false);
            try (PreparedStatement updateStatement = activeDatabaseConnection.prepareStatement(SQL_DEACTIVATE_MANAGER)) {
                for (Integer currentIdentifier : managerIdentifiersList) {
                    updateStatement.setString(1, UserStatus.INACTIVE.getDatabaseValue());
                    updateStatement.setInt(2, currentIdentifier);
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
                throw new DAOException("Error al ejecutar la inactivación masiva de encargados.", executionException);
            } finally {
                activeDatabaseConnection.setAutoCommit(true);
            }
        } catch (SQLException connectionException) {
            throw new DAOException("Error de conexión al procesar inactivación de encargados.", connectionException);
        }
        return allUpdatesSuccessful;
    }
}