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
import java.util.List;

/**
 * Acceso a datos de las postulaciones y asignaciones de proyecto de los practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class PostulationDAO extends BaseDAO implements IPostulationDAO {

    private static final String SQL_CHECK_EXISTING_POSTULATIONS =
            "SELECT COUNT(*) FROM project_postulation WHERE practitioner_id = ? AND postulation_status <> 'Cancelled'";
    private static final String SQL_INSERT_POSTULATION =
            "INSERT INTO project_postulation (practitioner_id, project_id, priority_level, postulation_status) VALUES (?, ?, ?, 'Pending')";
    private static final String SQL_CANCEL_PENDING_POSTULATIONS =
            "UPDATE project_postulation SET postulation_status = 'Cancelled' WHERE practitioner_id = ? AND postulation_status = 'Pending'";
    private static final String SQL_SELECT_POSTULATIONS_BY_PRACTITIONER =
            "SELECT DISTINCT p.practitioner_id, p.project_id, pr.project_name, p.priority_level, p.postulation_status " +
                    "FROM project_postulation p INNER JOIN project pr ON p.project_id = pr.project_id " +
                    "WHERE p.practitioner_id = ? AND p.postulation_status <> 'Cancelled' AND (p.postulation_status = 'Assigned' OR pr.participant_capacity > " +
                    "(SELECT COUNT(*) FROM project_postulation WHERE project_id = pr.project_id AND postulation_status = 'Assigned')) " +
                    "ORDER BY p.priority_level ASC";
    private static final String SQL_CHECK_ASSIGNED_PROJECT =
            "SELECT COUNT(*) FROM project_postulation WHERE practitioner_id = ? AND postulation_status = 'Assigned'";
    private static final String SQL_CALL_ASSIGNMENT_PROCEDURE =
            "{CALL assign_project_and_reject_others(?, ?)}";

    /**
     * Crea el DAO de postulaciones con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public PostulationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Indica si el practicante ya ha registrado sus prioridades de proyecto (postulaciones no canceladas).
     *
     * @param practitionerId identificador del practicante
     * @return {@code true} si tiene postulaciones vigentes; {@code false} en caso contrario
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public boolean hasPractitionerSubmittedPriorities(int practitionerId) throws DAOException {
        boolean hasSubmittedPriorities = false;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_CHECK_EXISTING_POSTULATIONS)) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    hasSubmittedPriorities = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Ocurrió un error al verificar el estado de las postulaciones del practicante.", e);
        }

        return hasSubmittedPriorities;
    }

    /**
     * Registra en lote las prioridades de proyecto de un practicante, cancelando antes las pendientes.
     *
     * @param practitionerId      identificador del practicante
     * @param prioritizedProjects proyectos en orden de prioridad descendente
     * @throws DAOException si ocurre un error y se revierte la transacción
     */
    @Override
    public void insertProjectPriorities(int practitionerId, List<Project> prioritizedProjects) throws DAOException {
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                executePriorityBatch(connection, practitionerId, prioritizedProjects);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("Ocurrió un error SQL durante la inserción en lote de las prioridades.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Fallo crítico de conexión con la base de datos al intentar guardar la postulación.", e);
        }
    }

    /**
     * Cancela las postulaciones pendientes e inserta por lotes las nuevas prioridades del practicante.
     *
     * @param connection     conexión transaccional sobre la que se ejecuta el lote
     * @param practitionerId identificador del practicante
     * @param projects       proyectos en orden de prioridad descendente
     * @throws SQLException si el lote no registra todas las prioridades o si ocurre un error
     */
    private void executePriorityBatch(Connection connection, int practitionerId, List<Project> projects) throws SQLException {
        cancelPendingPostulations(connection, practitionerId);
        try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_POSTULATION)) {
            for (int priorityIndex = 0; priorityIndex < projects.size(); priorityIndex++) {
                statement.setInt(1, practitionerId);
                statement.setInt(2, projects.get(priorityIndex).getProjectId());
                statement.setInt(3, priorityIndex + 1);
                statement.addBatch();
            }
            int[] batchResults = statement.executeBatch();
            for (int result : batchResults) {
                if (result == java.sql.Statement.EXECUTE_FAILED) {
                    throw new SQLException("No se registraron todas las prioridades de la postulación.");
                }
            }
        }
    }

    /**
     * Cancela las postulaciones pendientes de un practicante sobre la conexión recibida.
     *
     * @param connection     conexión transaccional sobre la que se ejecuta la actualización
     * @param practitionerId identificador del practicante
     * @throws SQLException si ocurre un error al ejecutar la actualización
     */
    private void cancelPendingPostulations(Connection connection, int practitionerId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_CANCEL_PENDING_POSTULATIONS)) {
            statement.setInt(1, practitionerId);
            statement.executeUpdate();
        }
    }

    /**
     * Recupera las postulaciones vigentes de un practicante, ordenadas por nivel de prioridad.
     *
     * @param practitionerId identificador del practicante
     * @return lista de postulaciones del practicante; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<ProjectPostulation> retrievePractitionerPostulations(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_POSTULATIONS_BY_PRACTITIONER, this::mapResultSetToPostulation, practitionerId);
    }

    /**
     * Asigna un proyecto a un practicante mediante un procedimiento almacenado que rechaza las demás postulaciones.
     *
     * @param practitionerId identificador del practicante
     * @param projectId      identificador del proyecto a asignar
     * @throws DAOException si ocurre un error al ejecutar el procedimiento almacenado
     */
    @Override
    public void assignProjectUsingStoredProcedure(int practitionerId, int projectId) throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
             CallableStatement statement = connection.prepareCall(SQL_CALL_ASSIGNMENT_PROCEDURE)) {

            statement.setInt(1, practitionerId);
            statement.setInt(2, projectId);
            statement.execute();

        } catch (SQLException e) {
            throw new DAOException("Ocurrió un error en el servidor al intentar ejecutar la asignación automática.", e);
        }
    }

    /**
     * Indica si el practicante ya tiene un proyecto asignado.
     *
     * @param practitionerId identificador del practicante
     * @return {@code true} si tiene un proyecto asignado; {@code false} en caso contrario
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public boolean hasAssignedProject(int practitionerId) throws DAOException {
        boolean hasAssignedProject = false;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_CHECK_ASSIGNED_PROJECT)) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    hasAssignedProject = resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al verificar el proyecto asignado del practicante.", e);
        }

        return hasAssignedProject;
    }

    /**
     * Construye una postulación con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return postulación con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private ProjectPostulation mapResultSetToPostulation(ResultSet resultSet) throws SQLException {
        ProjectPostulation postulation = new ProjectPostulation();
        postulation.setPractitionerId(resultSet.getInt("practitioner_id"));
        postulation.setProjectId(resultSet.getInt("project_id"));
        postulation.setProjectName(resultSet.getString("project_name"));
        postulation.setPriorityLevel(resultSet.getInt("priority_level"));
        postulation.setPostulationStatus(resultSet.getString("postulation_status"));
        return postulation;
    }
}