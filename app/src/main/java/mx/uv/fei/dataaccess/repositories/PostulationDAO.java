package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
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

    private static final String SQL_CHECK_EXISTING_POSTULATIONS = "SELECT COUNT(*) FROM project_postulation WHERE practitioner_id = ?";
    private static final String SQL_INSERT_POSTULATION = "INSERT INTO project_postulation (practitioner_id, project_id, priority_level, postulation_status) VALUES (?, ?, ?, 'Pending')";
    private static final String SQL_SELECT_POSTULATIONS = "SELECT p.practitioner_id, p.project_id, pr.project_name, p.priority_level, p.postulation_status FROM project_postulation p INNER JOIN project pr ON p.project_id = pr.project_id WHERE p.practitioner_id = ? AND (p.postulation_status = 'Assigned' OR pr.participant_capacity > (SELECT COUNT(*) FROM project_postulation WHERE project_id = pr.project_id AND postulation_status = 'Assigned')) ORDER BY p.priority_level ASC";
    private static final String SQL_CALL_ASSIGNMENT_PROCEDURE = "{CALL assign_project_and_reject_others(?, ?)}";
    private static final String SQL_HAS_ASSIGNED_PROJECT =
            "SELECT COUNT(*) FROM project_postulation " +
                    "WHERE practitioner_id = ? AND postulation_status = 'Assigned'";

    @Inject
    public PostulationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public boolean hasPractitionerSubmittedPriorities(int practitionerIdentifier) throws DAOException {
        boolean hasSubmittedPriorities = false;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement countStatement = connection.prepareStatement(SQL_CHECK_EXISTING_POSTULATIONS)) {

            countStatement.setInt(1, practitionerIdentifier);

            try (ResultSet resultSet = countStatement.executeQuery()) {
                if (resultSet.next()) {
                    hasSubmittedPriorities = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Ocurrió un error al verificar el estado de las postulaciones del practicante.", exception);
        }

        return hasSubmittedPriorities;
    }

    @Override
    public boolean insertProjectPriorities(int targetPractitionerIdentifier, List<Project> prioritizedProjectList) throws DAOException {
        boolean isBatchExecutionSuccessful = false;

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                isBatchExecutionSuccessful = executePriorityBatch(connection, targetPractitionerIdentifier, prioritizedProjectList);

                if (isBatchExecutionSuccessful) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException exception) {
                connection.rollback();
                throw new DAOException("Ocurrió un error SQL durante la inserción en lote de las prioridades.", exception);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw new DAOException("Fallo crítico de conexión con la base de datos al intentar guardar la postulación.", exception);
        }

        return isBatchExecutionSuccessful;
    }

    private boolean executePriorityBatch(Connection connection, int practitionerId, List<Project> projects) throws SQLException {
        boolean batchSuccessful = false;

        try (PreparedStatement batchInsertStatement = connection.prepareStatement(SQL_INSERT_POSTULATION)) {
            for (int priorityIndex = 0; priorityIndex < projects.size(); priorityIndex++) {
                batchInsertStatement.setInt(1, practitionerId);
                batchInsertStatement.setInt(2, projects.get(priorityIndex).getProjectId());
                batchInsertStatement.setInt(3, priorityIndex + 1);
                batchInsertStatement.addBatch();
            }
            int[] batchExecutionResultsArray = batchInsertStatement.executeBatch();
            batchSuccessful = batchExecutionResultsArray.length == projects.size();
        }

        return batchSuccessful;
    }

    @Override
    public List<ProjectPostulation> retrievePractitionerPostulations(int practitionerIdentifier) throws DAOException {
        List<ProjectPostulation> retrievedPostulationsList = new ArrayList<>();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(SQL_SELECT_POSTULATIONS)) {

            selectStatement.setInt(1, practitionerIdentifier);

            try (ResultSet resultSet = selectStatement.executeQuery()) {
                while (resultSet.next()) {
                    retrievedPostulationsList.add(mapResultSetToPostulation(resultSet));
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Ocurrió un error al intentar recuperar las postulaciones del practicante.", exception);
        }

        return retrievedPostulationsList;
    }

    private ProjectPostulation mapResultSetToPostulation(ResultSet resultSet) throws SQLException {
        ProjectPostulation postulation = new ProjectPostulation();

        postulation.setPractitionerIdentifier(resultSet.getInt("practitioner_id"));
        postulation.setProjectIdentifier(resultSet.getInt("project_id"));
        postulation.setProjectName(resultSet.getString("project_name"));
        postulation.setPriorityLevel(resultSet.getInt("priority_level"));
        postulation.setPostulationStatus(resultSet.getString("postulation_status"));

        return postulation;
    }

    @Override
    public boolean assignProjectUsingStoredProcedure(int practitionerIdentifier, int projectIdentifier) throws DAOException {
        boolean isProcedureExecutionSuccessful = false;

        try (Connection connection = databaseConnection.getConnection();
             CallableStatement assignmentProcedureStatement = connection.prepareCall(SQL_CALL_ASSIGNMENT_PROCEDURE)) {

            assignmentProcedureStatement.setInt(1, practitionerIdentifier);
            assignmentProcedureStatement.setInt(2, projectIdentifier);
            assignmentProcedureStatement.execute();

            isProcedureExecutionSuccessful = true;

        } catch (SQLException exception) {
            throw new DAOException("Ocurrió un error en el servidor al intentar ejecutar la asignación automática.", exception);
        }

        return isProcedureExecutionSuccessful;
    }

    @Override
    public boolean hasAssignedProject(int practitionerId) throws DAOException {
        boolean hasAssigned = false;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_HAS_ASSIGNED_PROJECT)) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    hasAssigned = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al verificar el proyecto asignado del practicante.", exception);
        }

        return hasAssigned;
    }
}