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

    private static final String SQL_INSERT = "INSERT INTO ORGANIZACION_VINCULADA (NOMBRE_ORGANIZACION, ESTADO, DIRECCION, CIUDAD, SECTOR, CORREO, TELEFONO) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT = "SELECT ID_ORGANIZACION, NOMBRE_ORGANIZACION ,ESTADO, DIRECCION, CIUDAD, SECTOR, CORREO, TELEFONO FROM ORGANIZACION_VINCULADA WHERE NOMBRE_ORGANIZACION = ?";
    private static final String SQL_SELECTALL = "SELECT * FROM ORGANIZACION_VINCULADA";
    private static final String SQL_UPDATE = "UPDATE ORGANIZACION_VINCULADA SET NOMBRE_ORGANIZACION = ?, ESTADO = ?, DIRECCION = ?, CIUDAD = ?, SECTOR = ?, CORREO = ?, TELEFONO = ? WHERE ID_ORGANIZACION = ?";

    private static final String SQL_DEACTIVATE_ORGANIZATION = "UPDATE ORGANIZACION_VINCULADA SET ESTADO = 'No Activo' WHERE ID_ORGANIZACION = ?";

    @Inject
    public OrganizationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    public boolean insertOrganization(Organization organization) throws DAOException {
        try (
                Connection connection = databaseConnection.getConnection();
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
        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT)

        ) {
            statement.setString(1, organizationName);
            try (
                    ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    organizationToSearch = new Organization();
                    organizationToSearch.setIdOrganization(resultSet.getInt("ID_ORGANIZACION"));
                    organizationToSearch.setNameOrganization(resultSet.getString("NOMBRE_ORGANIZACION"));
                    organizationToSearch.setState(resultSet.getString("ESTADO"));
                    organizationToSearch.setAdress(resultSet.getString("DIRECCION"));
                    organizationToSearch.setCity(resultSet.getString("CIUDAD"));
                    organizationToSearch.setBusiness(resultSet.getString("SECTOR"));
                    organizationToSearch.setMail(resultSet.getString("CORREO"));
                    organizationToSearch.setCellphone(resultSet.getString("TELEFONO"));
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
            organizationRecover.setIdOrganization(resultSet.getInt("ID_ORGANIZACION"));
            organizationRecover.setNameOrganization(resultSet.getString("NOMBRE_ORGANIZACION"));
            organizationRecover.setState(resultSet.getString("ESTADO"));
            organizationRecover.setAdress(resultSet.getString("DIRECCION"));
            organizationRecover.setCity(resultSet.getString("CIUDAD"));
            organizationRecover.setBusiness(resultSet.getString("SECTOR"));
            organizationRecover.setMail(resultSet.getString("CORREO"));
            organizationRecover.setCellphone(resultSet.getString("TELEFONO"));

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