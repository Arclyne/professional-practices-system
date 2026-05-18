package mx.uv.fei.dataacces.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IPostulationDAO;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.ProjectPostulation;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PostulationDAO extends BaseDAO implements IPostulationDAO {

    private static final String SQL_CHECK_EXISTING_POSTULATIONS = "SELECT COUNT(*) FROM project_application WHERE ID_PRACTITIONER = ?";
    private static final String SQL_INSERT_POSTULATION = "INSERT INTO project_application (ID_PRACTITIONER, ID_PROJECT, PRIORITY_LEVEL, APPLICATION_STATUS) VALUES (?, ?, ?, 'Pending')";
    private static final String SQL_SELECT_POSTULATIONS = "SELECT p.ID_PRACTITIONER, p.ID_PROJECT, pr.PROJECT_NAME, p.PRIORITY_LEVEL, p.APPLICATION_STATUS FROM project_application p INNER JOIN project pr ON p.ID_PROJECT = pr.ID_PROJECT WHERE p.ID_PRACTITIONER = ? AND (p.APPLICATION_STATUS = 'Assigned' OR pr.VACANCIES > (SELECT COUNT(*) FROM project_application WHERE ID_PROJECT = pr.ID_PROJECT AND APPLICATION_STATUS = 'Assigned')) ORDER BY p.PRIORITY_LEVEL ASC";
    private static final String SQL_CALL_ASSIGNMENT_PROCEDURE = "{CALL assign_project_and_reject_others(?, ?)}";

    @Inject
    public PostulationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public boolean hasPractitionerSubmittedPriorities(int practitionerIdentifier) throws DAOException {
        boolean hasSubmittedPriorities = false;
        try (Connection currentDatabaseConnection = databaseConnection.getConnection();
             PreparedStatement countStatement = currentDatabaseConnection.prepareStatement(SQL_CHECK_EXISTING_POSTULATIONS)) {
            countStatement.setInt(1, practitionerIdentifier);
            try (ResultSet executionResultSet = countStatement.executeQuery()) {
                if (executionResultSet.next()) {
                    hasSubmittedPriorities = executionResultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException executionException) {
            throw new DAOException("Ocurrio un error al verificar el estado de las postulaciones del practicante.", executionException);
        }
        return hasSubmittedPriorities;
    }

    @Override
    public boolean insertProjectPriorities(int targetPractitionerIdentifier, List<Project> prioritizedProjectList) throws DAOException {
        boolean isBatchExecutionSuccessful = false;
        try (Connection currentDatabaseConnection = databaseConnection.getConnection()) {
            currentDatabaseConnection.setAutoCommit(false);
            try (PreparedStatement batchInsertStatement = currentDatabaseConnection.prepareStatement(SQL_INSERT_POSTULATION)) {
                for (int priorityIndex = 0; priorityIndex < prioritizedProjectList.size(); priorityIndex++) {
                    batchInsertStatement.setInt(1, targetPractitionerIdentifier);
                    batchInsertStatement.setInt(2, prioritizedProjectList.get(priorityIndex).getProjectId());
                    batchInsertStatement.setInt(3, priorityIndex + 1);
                    batchInsertStatement.addBatch();
                }
                int[] batchExecutionResultsArray = batchInsertStatement.executeBatch();
                currentDatabaseConnection.commit();
                isBatchExecutionSuccessful = batchExecutionResultsArray.length == prioritizedProjectList.size();
            } catch (SQLException executionException) {
                currentDatabaseConnection.rollback();
                throw new DAOException("Ocurrio un error SQL durante la insercion en lote de las prioridades.", executionException);
            } finally {
                currentDatabaseConnection.setAutoCommit(true);
            }
        } catch (SQLException connectionException) {
            throw new DAOException("Fallo critico de conexion con la base de datos al intentar guardar la postulacion.", connectionException);
        }
        return isBatchExecutionSuccessful;
    }

    @Override
    public List<ProjectPostulation> retrievePractitionerPostulations(int practitionerIdentifier) throws DAOException {
        List<ProjectPostulation> retrievedPostulationsList = new ArrayList<>();
        try (Connection currentDatabaseConnection = databaseConnection.getConnection();
             PreparedStatement selectStatement = currentDatabaseConnection.prepareStatement(SQL_SELECT_POSTULATIONS)) {
            selectStatement.setInt(1, practitionerIdentifier);
            try (ResultSet executionResultSet = selectStatement.executeQuery()) {
                while (executionResultSet.next()) {
                    ProjectPostulation currentPostulation = new ProjectPostulation();
                    currentPostulation.setPractitionerIdentifier(executionResultSet.getInt("ID_PRACTITIONER"));
                    currentPostulation.setProjectIdentifier(executionResultSet.getInt("ID_PROJECT"));
                    currentPostulation.setProjectName(executionResultSet.getString("PROJECT_NAME"));
                    currentPostulation.setPriorityLevel(executionResultSet.getInt("PRIORITY_LEVEL"));
                    currentPostulation.setPostulationStatus(executionResultSet.getString("APPLICATION_STATUS"));
                    retrievedPostulationsList.add(currentPostulation);
                }
            }
        } catch (SQLException executionException) {
            throw new DAOException("Ocurrio un error al intentar recuperar las postulaciones del practicante.", executionException);
        }
        return retrievedPostulationsList;
    }

    @Override
    public boolean assignProjectUsingStoredProcedure(int practitionerIdentifier, int projectIdentifier) throws DAOException {
        boolean isProcedureExecutionSuccessful = false;
        try (Connection currentDatabaseConnection = databaseConnection.getConnection();
             CallableStatement assignmentProcedureStatement = currentDatabaseConnection.prepareCall(SQL_CALL_ASSIGNMENT_PROCEDURE)) {
            assignmentProcedureStatement.setInt(1, practitionerIdentifier);
            assignmentProcedureStatement.setInt(2, projectIdentifier);
            assignmentProcedureStatement.execute();
            isProcedureExecutionSuccessful = true;
        } catch (SQLException executionException) {
            throw new DAOException("Ocurrio un error en el servidor al intentar ejecutar la asignacion automatica.", executionException);
        }
        return isProcedureExecutionSuccessful;
    }
}