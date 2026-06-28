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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de los proyectos de prácticas profesionales.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
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
    private static final String SQL_SELECT_ASSIGNED_COUNTS_BY_PROJECT =
            "SELECT project_id, COUNT(*) AS assigned_count FROM project_postulation " +
                    "WHERE postulation_status = 'Assigned' GROUP BY project_id";

    /**
     * Crea el DAO de proyectos con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public ProjectDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta un nuevo proyecto y devuelve su identificador generado.
     *
     * @param project proyecto con los datos a registrar
     * @return identificador generado para el proyecto, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar el proyecto
     */
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

    /**
     * Recupera un proyecto a partir de su nombre y del encargado al que pertenece.
     *
     * @param projectName nombre del proyecto a recuperar
     * @param managerId   identificador del encargado del proyecto
     * @return proyecto encontrado, o un {@link Project} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Recupera todos los proyectos registrados.
     *
     * @return lista con todos los proyectos; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Project> getAllProjects() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_PROJECTS, this::mapResultSetToProject);
    }

    /**
     * Actualiza los datos de un proyecto existente.
     *
     * @param projectToUpdate proyecto con los datos modificados
     * @param projectId       identificador del proyecto a actualizar
     * @throws DAOException si el proyecto no existe o si ocurre un error al actualizar
     */
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

    /**
     * Recupera los proyectos activos que todavía tienen cupo disponible para practicantes.
     *
     * @return lista de proyectos con capacidad disponible; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Project> getAvailableProjectsWithCapacity() throws DAOException {
        return recoverALL(SQL_SELECT_AVAILABLE_PROJECTS_WITH_CAPACITY, this::mapResultSetToProject);
    }

    /**
     * Inactiva varios proyectos en una sola transacción por lotes.
     *
     * @param projectIds identificadores de los proyectos a inactivar
     * @throws DAOException si la operación por lotes falla o si ocurre un error de conexión
     */
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

    /**
     * Marca un proyecto como activo.
     *
     * @param projectId identificador del proyecto a activar
     * @throws DAOException si el proyecto no existe o si ocurre un error al actualizar
     */
    @Override
    public void activateProject(int projectId) throws DAOException {
        updateTuple(SQL_ACTIVATE_PROJECT, statement -> statement.setInt(1, projectId));
    }

    /**
     * Obtiene, por proyecto, la cantidad de practicantes con postulación asignada.
     *
     * @return mapa de identificador de proyecto a número de practicantes asignados
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public Map<Integer, Integer> getAssignedCountsByProject() throws DAOException {
        Map<Integer, Integer> assignedCountsByProject = new HashMap<>();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ASSIGNED_COUNTS_BY_PROJECT);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                assignedCountsByProject.put(resultSet.getInt("project_id"), resultSet.getInt("assigned_count"));
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar el conteo de practicantes asignados por proyecto.", e);
        }

        return assignedCountsByProject;
    }

    /**
     * Recupera el proyecto que tiene asignado un practicante.
     *
     * @param practitionerId identificador del practicante
     * @return proyecto asignado, o un {@link Project} vacío si no tiene ninguno
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Ejecuta por lotes la inactivación de los proyectos indicados sobre la conexión recibida.
     *
     * @param connection conexión transaccional sobre la que se ejecuta el lote
     * @param projectIds identificadores de los proyectos a inactivar
     * @throws SQLException si el lote no afecta a alguno de los proyectos o si ocurre un error
     */
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

    /**
     * Construye un proyecto con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return proyecto con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
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