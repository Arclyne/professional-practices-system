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

@Component
public class ManagerDAO extends BaseDAO implements IManagerDAO {

    private static final String SQL_SELECT_BY_ORG = "SELECT manager_id, manager_name, phone, email FROM project_manager WHERE organization_id = ?";
    private static final String SQL_INSERT_MANAGER = "INSERT INTO project_manager (manager_name, phone, email, organization_id) VALUES (?, ?, ?, ?)";

    @Inject
    public ManagerDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public List<Manager> getManagersByOrganization(int organizationId) throws DAOException {
        List<Manager> managersList = new ArrayList<>();

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ORG)
        ) {
            statement.setInt(1, organizationId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Manager manager = new Manager();
                    manager.setId(resultSet.getInt("manager_id"));
                    manager.setName(resultSet.getString("manager_name"));
                    manager.setPhone(resultSet.getString("phone"));
                    manager.setEmail(resultSet.getString("email"));
                    manager.setOrganizationId(organizationId);

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
        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT_MANAGER)
        ) {
            statement.setString(1, manager.getName());
            statement.setString(2, manager.getPhone());
            statement.setString(3, manager.getEmail());
            statement.setInt(4, manager.getOrganizationId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al intentar registrar el encargado en la base de datos.", e);
        }
    }
}