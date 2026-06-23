package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
public class OrganizationDAO extends BaseDAO implements IOrganizationDAO {

    private static final String SQL_INSERT_ORGANIZATION =
            "INSERT INTO linked_organization (organization_name, status, address, city, sector, email, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_ORGANIZATION =
            "UPDATE linked_organization SET organization_name = ?, status = ?, address = ?, city = ?, sector = ?, email = ?, phone = ? WHERE organization_id = ?";
    private static final String SQL_SELECT_ORGANIZATION_BY_NAME =
            "SELECT organization_id, organization_name, status, address, city, sector, email, phone FROM linked_organization WHERE organization_name = ?";
    private static final String SQL_SELECT_ALL_ORGANIZATIONS =
            "SELECT organization_id, organization_name, status, address, city, sector, email, phone FROM linked_organization";
    private static final String SQL_DEACTIVATE_ORGANIZATION =
            "UPDATE linked_organization SET status = 'Inactive' WHERE organization_id = ?";
    private static final String SQL_ACTIVATE_ORGANIZATION =
            "UPDATE linked_organization SET status = 'Active' WHERE organization_id = ?";

    @Inject
    public OrganizationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertOrganization(Organization organization) throws DAOException {
        return insertTuple(SQL_INSERT_ORGANIZATION, statement -> {
            statement.setString(1, organization.getNameOrganization());
            statement.setString(2, organization.getState());
            statement.setString(3, organization.getAdress());
            statement.setString(4, organization.getCity());
            statement.setString(5, organization.getBusiness());
            statement.setString(6, organization.getMail());
            statement.setString(7, organization.getCellphone());
        });
    }

    @Override
    public Organization recoverOrganization(String organizationName) throws DAOException {
        Organization recoveredOrganization = new Organization();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ORGANIZATION_BY_NAME)) {

            statement.setString(1, organizationName);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredOrganization = mapResultSetToOrganization(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar la organización en la base de datos.", e);
        }

        return recoveredOrganization;
    }

    @Override
    public List<Organization> getAllOrganizations() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_ORGANIZATIONS, this::mapResultSetToOrganization);
    }

    @Override
    public void updateOrganization(Organization organizationToUpdate, int organizationId) throws DAOException {
        updateTuple(SQL_UPDATE_ORGANIZATION, statement -> {
            statement.setString(1, organizationToUpdate.getNameOrganization());
            statement.setString(2, organizationToUpdate.getState());
            statement.setString(3, organizationToUpdate.getAdress());
            statement.setString(4, organizationToUpdate.getCity());
            statement.setString(5, organizationToUpdate.getBusiness());
            statement.setString(6, organizationToUpdate.getMail());
            statement.setString(7, organizationToUpdate.getCellphone());
            statement.setInt(8, organizationId);
        });
    }

    @Override
    public void deactivateMultipleOrganizations(List<Integer> organizationIds) throws DAOException {

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                executeDeactivationBatch(connection, organizationIds);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("Error al ejecutar la inactivación masiva de organizaciones.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error de conexión al procesar inactivación de organizaciones.", e);
        }
    }

    @Override
    public void activateOrganization(int organizationId) throws DAOException {
        updateTuple(SQL_ACTIVATE_ORGANIZATION, statement -> statement.setInt(1, organizationId));
    }

    private void executeDeactivationBatch(Connection connection, List<Integer> organizationIds) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DEACTIVATE_ORGANIZATION)) {
            for (Integer organizationId : organizationIds) {
                statement.setInt(1, organizationId);
                statement.addBatch();
            }
            statement.executeBatch();
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