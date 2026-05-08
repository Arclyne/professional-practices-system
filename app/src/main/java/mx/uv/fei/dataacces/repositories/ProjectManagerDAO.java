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
import mx.uv.fei.dataacces.interfaces.IProjectManagerDAO;
import mx.uv.fei.domain.dto.Manager;

@Component
public class ProjectManagerDAO extends BaseDAO implements IProjectManagerDAO {

    private static final String SQL_SELECT_BY_ORG = "SELECT ID_ENCARGADO, NOMBRE_ENCARGADO, TELEFONO, CORREO FROM ENCARGADO_PROYECTO WHERE ID_ORGANIZACION = ?";

    @Inject
    public ProjectManagerDAO(IDatabaseConnection databaseConnection) {
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
                    manager.setId(resultSet.getInt("ID_ENCARGADO"));
                    manager.setName(resultSet.getString("NOMBRE_ENCARGADO"));
                    manager.setPhone(resultSet.getString("TELEFONO"));
                    manager.setEmail(resultSet.getString("CORREO"));
                    manager.setOrganizationId(organizationId);

                    managersList.add(manager);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al consultar los encargados por organización en la base de datos.", e);
        }

        return managersList;
    }
}