package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Component
public class ProjectDAO extends BaseDAO implements IProjectDAO {

    private static final String SQL_INSERT_PROJECT =
            "INSERT INTO project (project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_PROJECT =
            "UPDATE project SET project_name = ?, description = ?, participant_capacity = ?, manager_id = ?, status = ?, start_date = ?, end_date = ?, organization_id = ? WHERE project_id = ?";
    private static final String SQL_SELECT_PROJECT_BY_NAME_AND_MANAGER =
            "SELECT project_id, project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id FROM project WHERE project_name = ? AND manager_id = ?";
    private static final String SQL_SELECT_ALL_PROJECTS =
            "SELECT project_id, project_name, description, participant_capacity, manager_id, status, start_date, end_date, organization_id FROM project";
    private static final String SQL_SELECT_AVAILABLE_PROJECTS_WITH_CAPACITY =
            "SELECT p.project_id, p.project_name, p.description, p.participant_capacity, p.manager_id, p.status, p.start_date, p.end_date, p.organization_id " +
                    "FROM project p " +
                    "WHERE p.status = 'Active' " +
                    "AND (p.participant_capacity > (" +
                    "    SELECT COUNT(*) FROM project_postulation pa " +
                    "    WHERE pa.project_id = p.project_id AND pa.postulation_status = 'Assigned'" +
                    "))";
    private static final String SQL_DEACTIVATE_PROJECT =
            "UPDATE project SET status = 'Inactive' WHERE project_id = ?";
    private static final String SQL_ACTIVATE_PROJECT =
            "UPDATE project SET status = 'Active' WHERE project_id = ?";

    private static final String SQL_SELECT_ASSIGNED_PROJECT_BY_PRACTITIONER =
            "SELECT p.project_id, p.project_name, p.description, p.participant_capacity, p.manager_id, p.status, p.start_date, p.end_date, p.organization_id " +
                    "FROM project p " +
                    "INNER JOIN project_postulation pp ON p.project_id = pp.project_id " +
                    "WHERE pp.practitioner_id = ? AND pp.postulation_status = 'Assigned'";

    @Inject
    public ProjectDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertProject(Project project) throws DAOException {
        return insertTuple(SQL_INSERT_PROJECT, statement -> {
            statement.setString(1, project.getProjectName());
            statement.setString(2, project.getDescription());
            statement.setInt(3, project.getParticipantCapacity());
            statement.setInt(4, project.getManagerId());
            statement.setString(5, project.getStatus());
            statement.setDate(6, project.getStartDate());
            statement.setDate(7, project.getEndDate());
            statement.setInt(8, project.getCompanyId());
        });
    }

    @Override
    public Project recoverProject(String projectName, int managerId) throws DAOException {
        Project recoveredProject = new Project();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_PROJECT_BY_NAME_AND_MANAGER)) {

            statement.setString(1, projectName);
            statement.setInt(2, managerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredProject = mapResultSetToProject(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el proyecto en la base de datos.", e);
        }

        return recoveredProject;
    }

    @Override
    public List<Project> getAllProjects() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_PROJECTS, this::mapResultSetToProject);
    }

    @Override
    public void updateProject(Project projectToUpdate, int projectId) throws DAOException {
        updateTuple(SQL_UPDATE_PROJECT, statement -> {
            statement.setString(1, projectToUpdate.getProjectName());
            statement.setString(2, projectToUpdate.getDescription());
            statement.setInt(3, projectToUpdate.getParticipantCapacity());
            statement.setInt(4, projectToUpdate.getManagerId());
            statement.setString(5, projectToUpdate.getStatus());
            statement.setDate(6, projectToUpdate.getStartDate());
            statement.setDate(7, projectToUpdate.getEndDate());
            statement.setInt(8, projectToUpdate.getCompanyId());
            statement.setInt(9, projectId);
        });
    }

    @Override
    public List<Project> getAvailableProjectsWithCapacity() throws DAOException {
        return recoverALL(SQL_SELECT_AVAILABLE_PROJECTS_WITH_CAPACITY, this::mapResultSetToProject);
    }

    @Override
    public void deactivateMultipleProjects(List<Integer> projectIds) throws DAOException {
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                executeDeactivationBatch(connection, projectIds);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("Error al ejecutar la inactivación masiva de proyectos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error de conexión al procesar inactivación de proyectos.", e);
        }
    }

    @Override
    public void activateProject(int projectId) throws DAOException {
        updateTuple(SQL_ACTIVATE_PROJECT, statement -> statement.setInt(1, projectId));
    }

    @Override
    public Project getAssignedProjectByPractitioner(int practitionerId) throws DAOException {
        Project assignedProject = new Project();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ASSIGNED_PROJECT_BY_PRACTITIONER)) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    assignedProject = mapResultSetToProject(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar el proyecto asignado del practicante.", e);
        }

        return assignedProject;
    }

    private void executeDeactivationBatch(Connection connection, List<Integer> projectIds) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DEACTIVATE_PROJECT)) {
            for (Integer projectId : projectIds) {
                statement.setInt(1, projectId);
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            for (int result : batchResults) {
                if (result <= 0 && result != Statement.SUCCESS_NO_INFO) {
                    throw new SQLException("La inactivación masiva no afectó a uno de los proyectos seleccionados.");
                }
            }
        }
    }

    private Project mapResultSetToProject(ResultSet resultSet) throws SQLException {
        Project project = new Project();
        project.setProjectId(resultSet.getInt("project_id"));
        project.setProjectName(resultSet.getString("project_name"));
        project.setDescription(resultSet.getString("description"));
        project.setParticipantCapacity(resultSet.getInt("participant_capacity"));
        project.setManagerId(resultSet.getInt("manager_id"));
        project.setStatus(resultSet.getString("status"));
        project.setStartDate(resultSet.getDate("start_date"));
        project.setEndDate(resultSet.getDate("end_date"));
        project.setCompanyId(resultSet.getInt("organization_id"));
        return project;
    }
}