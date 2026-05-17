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

    private static final String SQL_INSERT = "INSERT INTO linked_organization (organization_name, status, address, city, sector, email, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT = "SELECT organization_id, organization_name, status, address, city, sector, email, phone FROM linked_organization WHERE organization_name = ?";
    private static final String SQL_SELECTALL = "SELECT * FROM linked_organization";
    private static final String SQL_UPDATE = "UPDATE linked_organization SET organization_name = ?, status = ?, address = ?, city = ?, sector = ?, email = ?, phone = ? WHERE organization_id = ?";

    @Inject
    public OrganizationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    public boolean insertOrganization(Organization organization) throws DAOException {
        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {

            statement.setString(1, organization.getNameOrganization());
            statement.setString(2, organization.getRegion());
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
        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT)

        ) {
            statement.setString(1, organizationName);
            try (
                    ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    organizationToSearch = new Organization();
                    organizationToSearch.setIdOrganization(resultSet.getInt("organization_id"));
                    organizationToSearch.setNameOrganization(resultSet.getString("organization_name"));
                    organizationToSearch.setRegion(resultSet.getString("status"));
                    organizationToSearch.setAdress(resultSet.getString("address"));
                    organizationToSearch.setCity(resultSet.getString("city"));
                    organizationToSearch.setBusiness(resultSet.getString("sector"));
                    organizationToSearch.setMail(resultSet.getString("email"));
                    organizationToSearch.setCellphone(resultSet.getString("phone"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la organización en la base de datos.", e);
        }
        return organizationToSearch;
    }

    @Override
    public List<Organization> getAllOrganization() throws DAOException {
        return recoverALL(SQL_SELECTALL, resultSet -> {
            Organization organizationRecover = new Organization();
            organizationRecover.setIdOrganization(resultSet.getInt("organization_id"));
            organizationRecover.setNameOrganization(resultSet.getString("organization_name"));
            organizationRecover.setRegion(resultSet.getString("status"));
            organizationRecover.setAdress(resultSet.getString("address"));
            organizationRecover.setCity(resultSet.getString("city"));
            organizationRecover.setBusiness(resultSet.getString("sector"));
            organizationRecover.setMail(resultSet.getString("email"));
            organizationRecover.setCellphone(resultSet.getString("phone"));

            return organizationRecover;
        });
    }

    @Override
    public boolean updateOrganization(Organization upDateOrganization, int ID) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, upDateOrganization.getNameOrganization());
            statement.setString(2, upDateOrganization.getRegion());
            statement.setString(3, upDateOrganization.getAdress());
            statement.setString(4, upDateOrganization.getCity());
            statement.setString(5, upDateOrganization.getBusiness());
            statement.setString(6, upDateOrganization.getMail());
            statement.setString(7, upDateOrganization.getCellphone());
            statement.setInt(8, ID);
        });
    }
}