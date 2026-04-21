package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;

public class ProjectDAO extends BaseDAO implements IProjectDAO {

    public ProjectDAO(IDatabaseConnection dbConnection) {
        super(dbConnection);
    }

    private static final String SQL_INSERT = "INSERT INTO PROYECTO (NOMBRE_PROYECTO, DESCRIPCION, CUPO_PARTICIPANTES, ENCARGADO, ESTADO, FECHA_INICIO, FECHA_END, ID_ORGANIZACION) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECTONE = "SELECT ID_PROYECTO, NOMBRE_PROYECTO, DESCRIPCION, CUPO_PARTICIPANTES, ENCARGADO, ESTADO, FECHA_INICIO, FECHA_END, ID_ORGANIZACION FROM PROYECTO WHERE NOMBRE_PROYECTO = ? AND ENCARGADO = ?";
    private static final String SQL_SELECTALL = "SELECT * FROM PROYECTO";
    private static final String SQL_UPDATE = "UPDATE PROYECTO SET NOMBRE_PROYECTO = ?, DESCRIPCION = ?, CUPO_PARTICIPANTES = ?, ENCARGADO = ?, ESTADO = ?, FECHA_INICIO = ?, FECHA_END = ?, ID_ORGANIZACION = ? WHERE ID_PROYECTO= ?";

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

    @Override
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
                    projectToSearch.setDescription(resultSet.getString("DESCRIPCION"));
                    projectToSearch.setParticipantCapacity(resultSet.getInt("CUPO_PARTICIPANTES"));
                    projectToSearch.setManager(resultSet.getString("ENCARGADO"));
                    projectToSearch.setStatus(resultSet.getString("ESTADO"));
                    projectToSearch.setStartDate(resultSet.getDate("FECHA_INICIO"));
                    projectToSearch.setEndDate(resultSet.getDate("FECHA_END"));
                    projectToSearch.setCompanyId(resultSet.getInt("ID_ORGANIZACION"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la organización en la base de datos.", e);
        }
        return projectToSearch;
    }

    @Override
    public List<Project> getAllProjects() throws DAOException {
        return recoverALL(SQL_SELECTALL, resultSet -> {
            Project projectRecovered = new Project();
            projectRecovered.setProjectId(resultSet.getInt("ID_PROYECTO"));
            projectRecovered.setProjectName(resultSet.getString("NOMBRE_PROYECTO"));
            projectRecovered.setDescription(resultSet.getString("DESCRIPCION"));
            projectRecovered.setParticipantCapacity(resultSet.getInt("CUPO_PARTICIPANTES"));
            projectRecovered.setManager(resultSet.getString("ENCARGADO"));
            projectRecovered.setStatus(resultSet.getString("ESTADO"));
            projectRecovered.setStartDate(resultSet.getDate("FECHA_INICIO"));
            projectRecovered.setEndDate(resultSet.getDate("FECHA_END"));
            projectRecovered.setCompanyId(resultSet.getInt("ID_ORGANIZACION"));

            return projectRecovered;
        });
    }

    @Override
    public boolean updateProject(Project projectToUpdate, int ID) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, projectToUpdate.getProjectName());
            statement.setString(2, projectToUpdate.getDescription());
            statement.setInt(3, projectToUpdate.getParticipantCapacity());
            statement.setString(4, projectToUpdate.getManager());
            statement.setString(5, projectToUpdate.getStatus());
            statement.setDate(6, projectToUpdate.getStartDate());
            statement.setDate(7, projectToUpdate.getEndDate());
            statement.setInt(8, projectToUpdate.getCompanyId());
            statement.setInt(9, ID);
        });
    }
}
