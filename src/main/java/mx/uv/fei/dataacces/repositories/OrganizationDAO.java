package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.exceptions.DAOException;

public class OrganizationDAO implements IOrganizationDAO {

    private static final String SQL_INSERT = "INSERT INTO EMPRESA_VINCULADA (NOMBRE_EMPRESA, ESTADO, DIRECCION, CIUDAD, SECTOR, CORREO, TELEFONO) VALUES (?, ?, ?, ?, ?, ?, ?)";

    public boolean insertOrganization(Organization organizacion) throws DAOException {
        try (
                Connection connection = DatabaseConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setString(1, organizacion.getNombreEmpresa());
            statement.setString(2, organizacion.getEstado());
            statement.setString(3, organizacion.getDireccion());
            statement.setString(4, organizacion.getCiudad());
            statement.setString(5, organizacion.getSector());
            statement.setString(6, organizacion.getCorreo());
            statement.setString(7, organizacion.getTelefono());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la organización en la base de datos.", e);
        }
    }
}