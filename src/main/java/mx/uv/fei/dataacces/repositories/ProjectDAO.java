package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;

@Repository
public class ProjectDAO implements IProjectDAO {
    private final IDatabaseConnection dbConnection;

    @Autowired
    public ProjectDAO(IDatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    private static final String SQL_INSERT = "INSERT INTO PROYECTO (NOMBRE_PROYECTO, DESCRIPCION, CUPO_PARTICIPANTES, ENCARGADO, ESTADO, FECHA_INICIO, FECHA_END, ID_EMPRESA) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public boolean insertProject(Project project) throws DAOException {
        try (
                Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setString(1, project.getProjectName());
            statement.setString(2, project.getDescription());
            statement.setInt(3, project.getParticipantCapacity());
            statement.setString(4, project.getManager());
            statement.setString(5, project.getStatus());
            statement.setDate(6, project.getStartDate());
            statement.setDate(7, project.getEndDate());
            statement.setInt(8, project.getCompanyId());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar el proyecto en la base de datos.", e);
        }
    }
}
