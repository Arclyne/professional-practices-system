package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Acceso a datos de las actividades de la bitácora de los practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class ActivityDAO extends BaseDAO implements IActivityDAO {

    private static final String SQL_INSERT =
            "INSERT INTO activity (practitioner_id, title, description, start_date, end_date, duration_hours) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE =
            "UPDATE activity SET title = ?, description = ?, start_date = ?, end_date = ?, duration_hours = ? WHERE activity_id = ?";
    private static final String SQL_SELECT_BY_PRACTITIONER =
            "SELECT * FROM activity WHERE practitioner_id = ? ORDER BY start_date DESC";
    private static final String SQL_SELECT_BY_REPORT =
            "SELECT * FROM activity WHERE report_id = ? ORDER BY start_date ASC";
    private static final String SQL_ASSIGN_TO_REPORT =
            "UPDATE activity SET report_id = ? WHERE activity_id = ?";
    private static final String SQL_REMOVE_FROM_REPORT =
            "UPDATE activity SET report_id = NULL WHERE activity_id = ?";

    /**
     * Crea el DAO de actividades con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public ActivityDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta una nueva actividad en la bitácora y devuelve su identificador generado.
     *
     * @param activity actividad con los datos a registrar
     * @return identificador generado para la actividad, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar la actividad
     */
    @Override
    public int insertActivity(Activity activity) throws DAOException {
        int generatedId = -1;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            bindActivityInsertParameters(statement, activity);

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al guardar la actividad en la base de datos.", e);
        }
        return generatedId;
    }

    /**
     * Actualiza los datos de una actividad existente.
     *
     * @param activity   actividad con los datos modificados
     * @param activityId identificador de la actividad a actualizar
     * @throws DAOException si la actividad no existe o si ocurre un error al actualizar
     */
    @Override
    public void updateActivity(Activity activity, int activityId) throws DAOException {
        updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, activity.getTitle());
            statement.setString(2, activity.getDescription());
            statement.setDate(3, activity.getStartDate());
            statement.setDate(4, activity.getEndDate());
            statement.setInt(5, activity.getDurationHours());
            statement.setInt(6, activityId);
        });
    }

    /**
     * Recupera las actividades de un practicante, de la más reciente a la más antigua.
     *
     * @param practitionerId identificador del practicante
     * @return lista de actividades del practicante; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Activity> getActivitiesByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_BY_PRACTITIONER, this::mapResultSetToActivity, practitionerId);
    }

    /**
     * Recupera las actividades asociadas a un reporte, de la más antigua a la más reciente.
     *
     * @param reportId identificador del reporte
     * @return lista de actividades del reporte; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Activity> getActivitiesByReport(int reportId) throws DAOException {
        return recoverALL(SQL_SELECT_BY_REPORT, this::mapResultSetToActivity, reportId);
    }

    /**
     * Asocia una actividad a un reporte.
     *
     * @param activityId identificador de la actividad
     * @param reportId   identificador del reporte al que se asocia
     * @throws DAOException si la actividad no existe o si ocurre un error al actualizar
     */
    @Override
    public void assignActivityToReport(int activityId, int reportId) throws DAOException {
        updateTuple(SQL_ASSIGN_TO_REPORT, statement -> {
            statement.setInt(1, reportId);
            statement.setInt(2, activityId);
        });
    }

    /**
     * Desvincula una actividad del reporte al que estaba asociada.
     *
     * @param activityId identificador de la actividad a desvincular
     * @throws DAOException si la actividad no existe o si ocurre un error al actualizar
     */
    @Override
    public void removeActivityFromReport(int activityId) throws DAOException {
        updateTuple(SQL_REMOVE_FROM_REPORT, statement -> {
            statement.setInt(1, activityId);
        });
    }

    /**
     * Enlaza los valores de una actividad a la sentencia de inserción.
     *
     * @param statement sentencia preparada sobre la que se enlazan los valores
     * @param activity  actividad de la que se obtienen los datos
     * @throws SQLException si ocurre un error al enlazar algún parámetro
     */
    private void bindActivityInsertParameters(PreparedStatement statement, Activity activity) throws SQLException {
        statement.setInt(1, activity.getPractitionerId());
        statement.setString(2, activity.getTitle());
        statement.setString(3, activity.getDescription());
        statement.setDate(4, activity.getStartDate());
        statement.setDate(5, activity.getEndDate());
        statement.setInt(6, activity.getDurationHours());
    }

    /**
     * Construye una actividad con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return actividad con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Activity mapResultSetToActivity(ResultSet resultSet) throws SQLException {
        Activity activity = new Activity();
        activity.setActivityId(resultSet.getInt("activity_id"));
        activity.setPractitionerId(resultSet.getInt("practitioner_id"));
        activity.setReportId(resolveNullableReportId(resultSet));
        activity.setTitle(resultSet.getString("title"));
        activity.setDescription(resultSet.getString("description"));
        activity.setStartDate(resultSet.getDate("start_date"));
        activity.setEndDate(resultSet.getDate("end_date"));
        activity.setDurationHours(resultSet.getInt("duration_hours"));
        return activity;
    }

    /**
     * Obtiene el identificador del reporte tolerando valores nulos en la columna.
     *
     * @param resultSet resultado posicionado en la fila a leer
     * @return identificador del reporte, o {@code null} si la actividad no está asociada a uno
     * @throws SQLException si ocurre un error al leer la columna
     */
    private Integer resolveNullableReportId(ResultSet resultSet) throws SQLException {
        int reportId = resultSet.getInt("report_id");
        return resultSet.wasNull() ? null : reportId;
    }
}