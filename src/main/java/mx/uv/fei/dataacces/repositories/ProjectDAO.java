package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    private static final String SQL_SELECTONE = "SELECT ID_PROYECTO, NOMBRE_PROYECTO, ENCARGADO, ID_ORGANIZACION FROM PROYECTO WHERE NOMBRE PROYECTO = ? AND ENCARGADO = ?";

    @Override
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

    public Project recoverProject(String projectName, String manager) throws DAOException {
        Project projectToSearch = new Project();

        try (
                Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECTONE)) {
            statement.setString(1, projectName);
            statement.setString(2, manager);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    projectToSearch.setProjectId(resultSet.getInt("ID_PROYECTO"));
                    projectToSearch.setProjectName(resultSet.getString("NOMBRE_PROYECTO"));
                    projectToSearch.setManager(resultSet.getString("ENCARGADO"));
                    projectToSearch.setCompanyId(resultSet.getInt("ID_ORGANIZACION"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la organización en la base de datos.", e);
        }
        return projectToSearch;
    }
}
