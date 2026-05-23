package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;

@Component
public class ProjectDAO extends BaseDAO implements IProjectDAO {

    private static final String SQL_INSERT = "INSERT INTO project (project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECTONE = "SELECT project_id, project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id FROM project WHERE project_name = ? AND manager_id = ?";
    private static final String SQL_SELECTALL = "SELECT project_id, project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id FROM project";
    private static final String SQL_UPDATE = "UPDATE project SET project_name = ?, description = ?, participant_capacity = ?, manager_id = ?, status = ?, start_date = ?, end_date = ?, organization_id = ? WHERE project_id = ?";
    private static final String SQL_DEACTIVATE_PROJECT = "UPDATE project SET status = 'Inactive' WHERE project_id = ?";
    private static final String SQL_SELECT_AVAILABLE_WITH_CAPACITY =
            "SELECT p.project_id, p.project_name, p.description, p.participant_capacity, p.manager_id, p.status, p.start_date, p.end_date, p.organization_id " +
                    "FROM project p " +
                    "WHERE p.status = 'Active' " +
                    "AND p.participant_capacity > (" +
                    "    SELECT COALESCE(COUNT(pa.postulation_id), 0) " +
                    "    FROM project_postulation pa " +
                    "    WHERE pa.project_id = p.project_id AND pa.postulation_status = 'Assigned'" +
                    ")";

    @Inject
    public ProjectDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public boolean insertProject(Project project) throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setString(1, project.getProjectName());
            statement.setString(2, project.getDescription());
            statement.setInt(3, project.getParticipantCapacity());
            statement.setInt(4, project.getManagerId());
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
    public Project recoverProject(String projectName, int managerId) throws DAOException {
        Project projectToSearch = new Project();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECTONE)) {
            statement.setString(1, projectName);
            statement.setInt(2, managerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    projectToSearch.setProjectId(resultSet.getInt("project_id"));
                    projectToSearch.setProjectName(resultSet.getString("project_name"));
                    projectToSearch.setDescription(resultSet.getString("description"));
                    projectToSearch.setParticipantCapacity(resultSet.getInt("participant_capacity"));
                    projectToSearch.setManagerId(resultSet.getInt("manager_id"));
                    projectToSearch.setStatus(resultSet.getString("status"));
                    projectToSearch.setStartDate(resultSet.getDate("start_date"));
                    projectToSearch.setEndDate(resultSet.getDate("end_date"));
                    projectToSearch.setCompanyId(resultSet.getInt("organization_id"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el proyecto en la base de datos.", e);
        }
        return projectToSearch;
    }

    @Override
    public List<Project> getAllProjects() throws DAOException {
        return recoverALL(SQL_SELECTALL, resultSet -> {
            Project projectRecovered = new Project();
            projectRecovered.setProjectId(resultSet.getInt("project_id"));
            projectRecovered.setProjectName(resultSet.getString("project_name"));
            projectRecovered.setDescription(resultSet.getString("description"));
            projectRecovered.setParticipantCapacity(resultSet.getInt("participant_capacity"));
            projectRecovered.setManagerId(resultSet.getInt("manager_id"));
            projectRecovered.setStatus(resultSet.getString("status"));
            projectRecovered.setStartDate(resultSet.getDate("start_date"));
            projectRecovered.setEndDate(resultSet.getDate("end_date"));
            projectRecovered.setCompanyId(resultSet.getInt("organization_id"));
            return projectRecovered;
        });
    }

    @Override
    public boolean updateProject(Project projectToUpdate, int ID) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, projectToUpdate.getProjectName());
            statement.setString(2, projectToUpdate.getDescription());
            statement.setInt(3, projectToUpdate.getParticipantCapacity());
            statement.setInt(4, projectToUpdate.getManagerId());
            statement.setString(5, projectToUpdate.getStatus());
            statement.setDate(6, projectToUpdate.getStartDate());
            statement.setDate(7, projectToUpdate.getEndDate());
            statement.setInt(8, projectToUpdate.getCompanyId());
            statement.setInt(9, ID);
        });
    }

    @Override
    public List<Project> getAvailableProjectsWithCapacity() throws DAOException {
        return recoverALL(SQL_SELECT_AVAILABLE_WITH_CAPACITY, resultSet -> {
            Project projectRecovered = new Project();
            projectRecovered.setProjectId(resultSet.getInt("project_id"));
            projectRecovered.setProjectName(resultSet.getString("project_name"));
            projectRecovered.setDescription(resultSet.getString("description"));
            projectRecovered.setParticipantCapacity(resultSet.getInt("participant_capacity"));
            projectRecovered.setManagerId(resultSet.getInt("manager_id"));
            projectRecovered.setStatus(resultSet.getString("status"));
            projectRecovered.setStartDate(resultSet.getDate("start_date"));
            projectRecovered.setEndDate(resultSet.getDate("end_date"));
            projectRecovered.setCompanyId(resultSet.getInt("organization_id"));
            return projectRecovered;
        });
    }

    @Override
    public boolean deactivateMultipleProjects(List<Integer> projectIdentifiersList) throws DAOException {
        boolean allUpdatesSuccessful = true;
        try (Connection activeDatabaseConnection = databaseConnection.getConnection()) {
            activeDatabaseConnection.setAutoCommit(false);
            try (PreparedStatement updateStatement = activeDatabaseConnection.prepareStatement(SQL_DEACTIVATE_PROJECT)) {
                for (Integer currentIdentifier : projectIdentifiersList) {
                    updateStatement.setInt(1, currentIdentifier);
                    updateStatement.addBatch();
                }
                int[] executionResults = updateStatement.executeBatch();
                for (int result : executionResults) {
                    if (result <= 0 && result != java.sql.Statement.SUCCESS_NO_INFO) {
                        allUpdatesSuccessful = false;
                        break;
                    }
                }
                if (allUpdatesSuccessful) {
                    activeDatabaseConnection.commit();
                } else {
                    activeDatabaseConnection.rollback();
                }
            } catch (SQLException executionException) {
                activeDatabaseConnection.rollback();
                throw new DAOException("Error al ejecutar la inactivación masiva de proyectos.", executionException);
            } finally {
                activeDatabaseConnection.setAutoCommit(true);
            }
        } catch (SQLException connectionException) {
            throw new DAOException("Error de conexión al procesar inactivación de proyectos.", connectionException);
        }
        return allUpdatesSuccessful;
    }
}