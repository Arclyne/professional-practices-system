package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;

@Component
public class OrganizationDAO extends BaseDAO implements IOrganizationDAO {

    private static final String SQL_INSERT_ORGANIZATION = "INSERT INTO linked_organization (organization_name, status, address, city, sector, email, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ORGANIZATION_BY_NAME = "SELECT organization_id, organization_name, status, address, city, sector, email, phone FROM linked_organization WHERE organization_name = ?";
    private static final String SQL_SELECT_ALL_ORGANIZATIONS = "SELECT organization_id, organization_name, status, address, city, sector, email, phone FROM linked_organization";
    private static final String SQL_UPDATE_ORGANIZATION = "UPDATE linked_organization SET organization_name = ?, status = ?, address = ?, city = ?, sector = ?, email = ?, phone = ? WHERE organization_id = ?";
    private static final String SQL_DEACTIVATE_ORGANIZATION = "UPDATE linked_organization SET status = 'Inactive' WHERE organization_id = ?";

    @Inject
    public OrganizationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public boolean insertOrganization(Organization organization) throws DAOException {
        boolean isInserted = false;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_ORGANIZATION)) {

            statement.setString(1, organization.getNameOrganization());
            statement.setString(2, organization.getState());
            statement.setString(3, organization.getAdress());
            statement.setString(4, organization.getCity());
            statement.setString(5, organization.getBusiness());
            statement.setString(6, organization.getMail());
            statement.setString(7, organization.getCellphone());

            isInserted = statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la organización en la base de datos.", e);
        }

        return isInserted;
    }

    @Override
    public Organization recoverOrganization(String organizationName) throws DAOException {
        Organization organizationToSearch = new Organization();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ORGANIZATION_BY_NAME)) {

            statement.setString(1, organizationName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    organizationToSearch = mapResultSetToOrganization(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar la organización en la base de datos.", e);
        }

        return organizationToSearch;
    }

    @Override
    public List<Organization> getAllOrganizations() throws DAOException {
        List<Organization> organizationsList;

        organizationsList = recoverALL(SQL_SELECT_ALL_ORGANIZATIONS, this::mapResultSetToOrganization);

        return organizationsList;
    }

    @Override
    public boolean updateOrganization(Organization updateOrganization, int organizationId) throws DAOException {
        boolean isUpdated = false;

        isUpdated = updateTuple(SQL_UPDATE_ORGANIZATION, statement -> {
            statement.setString(1, updateOrganization.getNameOrganization());
            statement.setString(2, updateOrganization.getState());
            statement.setString(3, updateOrganization.getAdress());
            statement.setString(4, updateOrganization.getCity());
            statement.setString(5, updateOrganization.getBusiness());
            statement.setString(6, updateOrganization.getMail());
            statement.setString(7, updateOrganization.getCellphone());
            statement.setInt(8, organizationId);
        });

        return isUpdated;
    }

    @Override
    public boolean deactivateMultipleOrganizations(List<Integer> organizationIdentifiersList) throws DAOException {
        boolean allUpdatesSuccessful = true;

        try (Connection activeDatabaseConnection = databaseConnection.getConnection()) {
            activeDatabaseConnection.setAutoCommit(false);

            try {
                executeDeactivationBatch(activeDatabaseConnection, organizationIdentifiersList);
                activeDatabaseConnection.commit();
            } catch (SQLException e) {
                activeDatabaseConnection.rollback();
                allUpdatesSuccessful = false;
                throw new DAOException("Error al ejecutar la inactivación masiva de organizaciones.", e);
            } finally {
                activeDatabaseConnection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error de conexión al procesar inactivación de organizaciones.", e);
        }

        return allUpdatesSuccessful;
    }

    private void executeDeactivationBatch(Connection connection, List<Integer> organizationIdentifiersList) throws SQLException {
        try (PreparedStatement updateStatement = connection.prepareStatement(SQL_DEACTIVATE_ORGANIZATION)) {
            for (Integer currentIdentifier : organizationIdentifiersList) {
                updateStatement.setInt(1, currentIdentifier);
                updateStatement.addBatch();
            }
            updateStatement.executeBatch();
        }
    }

    private Organization mapResultSetToOrganization(ResultSet resultSet) throws SQLException {
        Organization organization = new Organization();

        organization.setIdOrganization(resultSet.getInt("organization_id"));
        organization.setNameOrganization(resultSet.getString("organization_name"));
        organization.setState(resultSet.getString("status"));
        organization.setAdress(resultSet.getString("address"));
        organization.setCity(resultSet.getString("city"));
        organization.setBusiness(resultSet.getString("sector"));
        organization.setMail(resultSet.getString("email"));
        organization.setCellphone(resultSet.getString("phone"));

        return organization;
    }
}
