package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.dataacces.exceptions.DAOException;

public class OrganizationDAO implements IOrganizationDAO {

    private static final String SQL_INSERT = "INSERT INTO ORGANIZACION_VINCULADA (NOMBRE_ORGANIZACION, ESTADO, DIRECCION, CIUDAD, SECTOR, CORREO, TELEFONO) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECT = "SELECT ESTADO, DIRECCION, CIUDAD, SECTOR, CORREO, TELEFONO FROM ORGANIZACION_VINCULADA WHERE NOMBRE_ORGANIZACION = ?";

    public boolean insertOrganization(Organization organization) throws DAOException {
        try (
                Connection connection = DatabaseConnection.getInstance().getConnection();
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

    public Organization recoverOrganization(String nombreOrganizacion) throws DAOException {
        Organization orgTemporal = null;
        try (Connection connection = DatabaseConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT)) {
            statement.setString(1, nombreOrganizacion);
            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    orgTemporal = new Organization();
                    orgTemporal.setNameOrganization(nombreOrganizacion);
                    orgTemporal.setRegion(resultSet.getString("ESTADO"));
                    orgTemporal.setAdress(resultSet.getString("DIRECCION"));
                    orgTemporal.setCity(resultSet.getString("CIUDAD"));
                    orgTemporal.setBusiness(resultSet.getString("SECTOR"));
                    orgTemporal.setMail(resultSet.getString("CORREO"));
                    orgTemporal.setCellphone(resultSet.getString("TELEFONO"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la organización en la base de datos.", e);
        }
        return orgTemporal;
    }
}