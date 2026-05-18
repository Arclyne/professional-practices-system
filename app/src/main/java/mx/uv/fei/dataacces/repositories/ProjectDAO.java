package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;

@Component
public class ProjectDAO extends BaseDAO implements IProjectDAO {

    @Inject
    public ProjectDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    private static final String SQL_INSERT = "INSERT INTO project (PROJECT_NAME, DESCRIPTION, VACANCIES, ID_MANAGER, STATUS, START_DATE, END_DATE, ID_ORGANIZATION) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_SELECTONE = "SELECT ID_PROJECT, PROJECT_NAME, DESCRIPTION, VACANCIES, ID_MANAGER, STATUS, START_DATE, END_DATE, ID_ORGANIZATION FROM project WHERE PROJECT_NAME = ? AND ID_MANAGER = ?";
    private static final String SQL_SELECTALL = "SELECT * FROM project";
    private static final String SQL_UPDATE = "UPDATE project SET PROJECT_NAME = ?, DESCRIPTION = ?, VACANCIES = ?, ID_MANAGER = ?, STATUS = ?, START_DATE = ?, END_DATE = ?, ID_ORGANIZATION = ? WHERE ID_PROJECT = ?";

    private static final String SQL_DEACTIVATE_PROJECT = "UPDATE project SET STATUS = 'Inactive' WHERE ID_PROJECT = ?";

    private static final String SQL_SELECT_AVAILABLE_WITH_CAPACITY =
            "SELECT p.ID_PROJECT, p.PROJECT_NAME, p.DESCRIPTION, p.VACANCIES, p.ID_MANAGER, p.STATUS, p.START_DATE, p.END_DATE, p.ID_ORGANIZATION " +
                    "FROM project p " +
                    "LEFT JOIN project_application pa ON p.ID_PROJECT = pa.ID_PROJECT AND pa.APPLICATION_STATUS = 'Assigned' " +
                    "WHERE p.STATUS = 'Active' " +
                    "GROUP BY p.ID_PROJECT " +
                    "HAVING COUNT(pa.ID_PRACTITIONER) < p.VACANCIES";

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
                    projectToSearch.setProjectId(resultSet.getInt("ID_PROJECT"));
                    projectToSearch.setProjectName(resultSet.getString("PROJECT_NAME"));
                    projectToSearch.setDescription(resultSet.getString("DESCRIPTION"));
                    projectToSearch.setParticipantCapacity(resultSet.getInt("VACANCIES"));
                    projectToSearch.setManagerId(resultSet.getInt("ID_MANAGER"));
                    projectToSearch.setStatus(resultSet.getString("STATUS"));
                    projectToSearch.setStartDate(resultSet.getDate("START_DATE"));
                    projectToSearch.setEndDate(resultSet.getDate("END_DATE"));
                    projectToSearch.setCompanyId(resultSet.getInt("ID_ORGANIZATION"));
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
            projectRecovered.setProjectId(resultSet.getInt("ID_PROJECT"));
            projectRecovered.setProjectName(resultSet.getString("PROJECT_NAME"));
            projectRecovered.setDescription(resultSet.getString("DESCRIPTION"));
            projectRecovered.setParticipantCapacity(resultSet.getInt("VACANCIES"));
            projectRecovered.setManagerId(resultSet.getInt("ID_MANAGER"));
            projectRecovered.setStatus(resultSet.getString("STATUS"));
            projectRecovered.setStartDate(resultSet.getDate("START_DATE"));
            projectRecovered.setEndDate(resultSet.getDate("END_DATE"));
            projectRecovered.setCompanyId(resultSet.getInt("ID_ORGANIZATION"));
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
            projectRecovered.setProjectId(resultSet.getInt("ID_PROJECT"));
            projectRecovered.setProjectName(resultSet.getString("PROJECT_NAME"));
            projectRecovered.setDescription(resultSet.getString("DESCRIPTION"));
            projectRecovered.setParticipantCapacity(resultSet.getInt("VACANCIES"));
            projectRecovered.setManagerId(resultSet.getInt("ID_MANAGER"));
            projectRecovered.setStatus(resultSet.getString("STATUS"));
            projectRecovered.setStartDate(resultSet.getDate("START_DATE"));
            projectRecovered.setEndDate(resultSet.getDate("END_DATE"));
            projectRecovered.setCompanyId(resultSet.getInt("ID_ORGANIZATION"));
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