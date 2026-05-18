package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;

@Component
public class OrganizationDAO extends BaseDAO implements IOrganizationDAO {

    private static final String SQL_INSERT = "INSERT INTO linked_organization (ORGANIZATION_NAME, STATE, ADDRESS, CITY, SECTOR, EMAIL, PHONE) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT = "SELECT ID_ORGANIZATION, ORGANIZATION_NAME, STATE, ADDRESS, CITY, SECTOR, EMAIL, PHONE FROM linked_organization WHERE ORGANIZATION_NAME = ?";
    private static final String SQL_SELECTALL = "SELECT * FROM linked_organization";
    private static final String SQL_UPDATE = "UPDATE linked_organization SET ORGANIZATION_NAME = ?, STATE = ?, ADDRESS = ?, CITY = ?, SECTOR = ?, EMAIL = ?, PHONE = ? WHERE ID_ORGANIZATION = ?";

    private static final String SQL_DEACTIVATE_ORGANIZATION = "UPDATE linked_organization SET STATE = 'Inactive' WHERE ID_ORGANIZATION = ?";

    @Inject
    public OrganizationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    public boolean insertOrganization(Organization organization) throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setString(1, organization.getNameOrganization());
            statement.setString(2, organization.getState());
            statement.setString(3, organization.getAdress());
            statement.setString(4, organization.getCity());
            statement.setString(5, organization.getBusiness());
            statement.setString(6, organization.getMail());
            statement.setString(7, organization.getCellphone());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la organización en la base de datos.", e);
        }
    }

    @Override
    public Organization recoverOrganization(String organizationName) throws DAOException {
        Organization organizationToSearch = null;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT)) {
            statement.setString(1, organizationName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    organizationToSearch = new Organization();
                    organizationToSearch.setIdOrganization(resultSet.getInt("ID_ORGANIZATION"));
                    organizationToSearch.setNameOrganization(resultSet.getString("ORGANIZATION_NAME"));
                    organizationToSearch.setState(resultSet.getString("STATE"));
                    organizationToSearch.setAdress(resultSet.getString("ADDRESS"));
                    organizationToSearch.setCity(resultSet.getString("CITY"));
                    organizationToSearch.setBusiness(resultSet.getString("SECTOR"));
                    organizationToSearch.setMail(resultSet.getString("EMAIL"));
                    organizationToSearch.setCellphone(resultSet.getString("PHONE"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar la organización en la base de datos.", e);
        }
        return organizationToSearch;
    }

    @Override
    public List<Organization> getAllOrganization() throws DAOException {
        return recoverALL(SQL_SELECTALL, resultSet -> {
            Organization organizationRecover = new Organization();
            organizationRecover.setIdOrganization(resultSet.getInt("ID_ORGANIZATION"));
            organizationRecover.setNameOrganization(resultSet.getString("ORGANIZATION_NAME"));
            organizationRecover.setState(resultSet.getString("STATE"));
            organizationRecover.setAdress(resultSet.getString("ADDRESS"));
            organizationRecover.setCity(resultSet.getString("CITY"));
            organizationRecover.setBusiness(resultSet.getString("SECTOR"));
            organizationRecover.setMail(resultSet.getString("EMAIL"));
            organizationRecover.setCellphone(resultSet.getString("PHONE"));
            return organizationRecover;
        });
    }

    @Override
    public boolean updateOrganization(Organization upDateOrganization, int ID) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, upDateOrganization.getNameOrganization());
            statement.setString(2, upDateOrganization.getState());
            statement.setString(3, upDateOrganization.getAdress());
            statement.setString(4, upDateOrganization.getCity());
            statement.setString(5, upDateOrganization.getBusiness());
            statement.setString(6, upDateOrganization.getMail());
            statement.setString(7, upDateOrganization.getCellphone());
            statement.setInt(8, ID);
        });
    }

    @Override
    public boolean deactivateMultipleOrganizations(List<Integer> organizationIdentifiersList) throws DAOException {
        boolean allUpdatesSuccessful = true;
        try (Connection activeDatabaseConnection = databaseConnection.getConnection()) {
            activeDatabaseConnection.setAutoCommit(false);
            try (PreparedStatement updateStatement = activeDatabaseConnection.prepareStatement(SQL_DEACTIVATE_ORGANIZATION)) {
                for (Integer currentIdentifier : organizationIdentifiersList) {
                    updateStatement.setInt(1, currentIdentifier);
                    updateStatement.addBatch();
                }
                updateStatement.executeBatch();
                activeDatabaseConnection.commit();
            } catch (SQLException executionException) {
                activeDatabaseConnection.rollback();
                throw new DAOException("Error al ejecutar la inactivación masiva de organizaciones.", executionException);
            } finally {
                activeDatabaseConnection.setAutoCommit(true);
            }
        } catch (SQLException connectionException) {
            throw new DAOException("Error de conexión al procesar inactivación de organizaciones.", connectionException);
        }
        return allUpdatesSuccessful;
    }
}