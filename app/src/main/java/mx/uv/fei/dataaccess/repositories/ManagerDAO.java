package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IManagerDAO;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.enums.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Acceso a datos de los encargados de proyecto de las organizaciones.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class ManagerDAO extends BaseDAO implements IManagerDAO {

    private static final String SQL_INSERT_MANAGER =
            "INSERT INTO project_manager (manager_name, phone, email, status, organization_id) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL_MANAGERS =
            "SELECT manager_id, manager_name, phone, email, status, organization_id FROM project_manager";
    private static final String SQL_SELECT_MANAGERS_BY_ORGANIZATION =
            "SELECT manager_id, manager_name, phone, email, status, organization_id FROM project_manager WHERE organization_id = ?";
    private static final String SQL_DEACTIVATE_MANAGER =
            "UPDATE project_manager SET status = 'Inactive' WHERE manager_id = ?";
    private static final String SQL_ACTIVATE_MANAGER =
            "UPDATE project_manager SET status = 'Active' WHERE manager_id = ?";
    private static final String SQL_UPDATE_MANAGER =
            "UPDATE project_manager SET manager_name = ?, phone = ?, email = ?, organization_id = ? WHERE manager_id = ?";
    private static final String SQL_SELECT_MANAGER_BY_ID =
            "SELECT manager_id, manager_name, phone, email, status, organization_id FROM project_manager WHERE manager_id = ?";

    /**
     * Crea el DAO de encargados de proyecto con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public ManagerDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Recupera los encargados pertenecientes a una organización.
     *
     * @param organizationId identificador de la organización
     * @return lista de encargados de la organización; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Manager> getManagersByOrganization(int organizationId) throws DAOException {
        return recoverALL(SQL_SELECT_MANAGERS_BY_ORGANIZATION, this::mapResultSetToManager, organizationId);
    }

    /**
     * Inserta un nuevo encargado de proyecto y devuelve su identificador generado.
     *
     * @param manager encargado con los datos a registrar
     * @return identificador generado para el encargado, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar el encargado
     */
    @Override
    public int insertManager(Manager manager) throws DAOException {
        return insertTuple(SQL_INSERT_MANAGER, statement -> {
            statement.setString(1, manager.getName());
            statement.setString(2, manager.getPhone());
            statement.setString(3, manager.getEmail());
            statement.setString(4, manager.getStatus().getDatabaseValue());
            statement.setInt(5, manager.getOrganizationId());
        });
    }

    /**
     * Recupera todos los encargados de proyecto registrados.
     *
     * @return lista con todos los encargados; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Manager> getAllManagers() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_MANAGERS, this::mapResultSetToManager);
    }

    /**
     * Recupera un encargado de proyecto a partir de su identificador.
     *
     * @param managerId identificador del encargado a recuperar
     * @return encargado encontrado, o un {@link Manager} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public Manager recoverManager(int managerId) throws DAOException {
        List<Manager> managers = recoverALL(SQL_SELECT_MANAGER_BY_ID, this::mapResultSetToManager, managerId);
        return managers.isEmpty() ? new Manager() : managers.getFirst();
    }

    /**
     * Actualiza los datos de un encargado de proyecto existente.
     *
     * @param manager   encargado con los datos modificados
     * @param managerId identificador del encargado a actualizar
     * @throws DAOException si el encargado no existe o si ocurre un error al actualizar
     */
    @Override
    public void updateManager(Manager manager, int managerId) throws DAOException {
        updateTuple(SQL_UPDATE_MANAGER, statement -> {
            statement.setString(1, manager.getName());
            statement.setString(2, manager.getPhone());
            statement.setString(3, manager.getEmail());
            statement.setInt(4, manager.getOrganizationId());
            statement.setInt(5, managerId);
        });
    }

    /**
     * Marca un encargado de proyecto como activo.
     *
     * @param managerId identificador del encargado a activar
     * @throws DAOException si el encargado no existe o si ocurre un error al actualizar
     */
    @Override
    public void activateManager(int managerId) throws DAOException {
        updateTuple(SQL_ACTIVATE_MANAGER, statement -> statement.setInt(1, managerId));
    }

    /**
     * Inactiva varios encargados de proyecto en una sola transacción por lotes.
     *
     * @param managerIds identificadores de los encargados a inactivar
     * @throws DAOException si la operación por lotes falla o si ocurre un error de conexión
     */
    @Override
    public void deactivateMultipleManagers(List<Integer> managerIds) throws DAOException {
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                executeDeactivationBatch(connection, managerIds);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("Error al ejecutar la inactivación masiva de encargados.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Error de conexión al procesar inactivación de encargados.", e);
        }
    }

    /**
     * Ejecuta por lotes la inactivación de los encargados indicados sobre la conexión recibida.
     *
     * @param connection conexión transaccional sobre la que se ejecuta el lote
     * @param managerIds identificadores de los encargados a inactivar
     * @throws SQLException si el lote no afecta a alguno de los encargados o si ocurre un error
     */
    private void executeDeactivationBatch(Connection connection, List<Integer> managerIds) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SQL_DEACTIVATE_MANAGER)) {
            for (Integer managerId : managerIds) {
                statement.setInt(1, managerId);
                statement.addBatch();
            }

            int[] batchResults = statement.executeBatch();
            for (int result : batchResults) {
                if (result <= 0 && result != Statement.SUCCESS_NO_INFO) {
                    throw new SQLException("La inactivación masiva no afectó a uno de los encargados seleccionados.");
                }
            }
        }
    }

    /**
     * Construye un encargado con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return encargado con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Manager mapResultSetToManager(ResultSet resultSet) throws SQLException {
        Manager manager = new Manager();
        manager.setId(resultSet.getInt("manager_id"));
        manager.setName(resultSet.getString("manager_name"));
        manager.setPhone(resultSet.getString("phone"));
        manager.setEmail(resultSet.getString("email"));
        manager.setStatus(resolveNullableStatus(resultSet));
        manager.setOrganizationId(resultSet.getInt("organization_id"));
        return manager;
    }

    /**
     * Obtiene el estado del encargado tolerando valores nulos en la columna.
     *
     * @param resultSet resultado posicionado en la fila a leer
     * @return estado del encargado, o {@code null} si la columna es nula
     * @throws SQLException si ocurre un error al leer la columna
     */
    private UserStatus resolveNullableStatus(ResultSet resultSet) throws SQLException {
        String statusValue = resultSet.getString("status");
        return statusValue != null ? UserStatus.fromString(statusValue) : null;
    }
}