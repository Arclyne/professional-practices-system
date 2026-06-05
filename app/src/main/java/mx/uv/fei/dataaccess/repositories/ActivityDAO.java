package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;

@Component
public class ActivityDAO extends BaseDAO implements IActivityDAO {

    private static final String SQL_INSERT = "INSERT INTO activity (practitioner_id, title, description, activity_date, duration_hours) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE activity SET title = ?, description = ?, activity_date = ?, duration_hours = ? WHERE activity_id = ?";
    private static final String SQL_SELECT_BY_PRACTITIONER = "SELECT * FROM activity WHERE practitioner_id = ? ORDER BY activity_date DESC";
    private static final String SQL_SELECT_BY_REPORT = "SELECT * FROM activity WHERE report_id = ? ORDER BY activity_date ASC";
    private static final String SQL_ASSIGN_REPORT = "UPDATE activity SET report_id = ? WHERE activity_id = ?";
    private static final String SQL_REMOVE_REPORT = "UPDATE activity SET report_id = NULL WHERE activity_id = ?";

    @Inject
    public ActivityDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertActivity(Activity activity) throws DAOException {
        int generatedId = -1;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, activity.getPractitionerId());
            statement.setString(2, activity.getTitle());
            statement.setString(3, activity.getDescription());
            statement.setDate(4, activity.getActivityDate());
            statement.setInt(5, activity.getDurationHours());

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

    @Override
    public boolean updateActivity(Activity activity, int activityId) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, activity.getTitle());
            statement.setString(2, activity.getDescription());
            statement.setDate(3, activity.getActivityDate());
            statement.setInt(4, activity.getDurationHours());
            statement.setInt(5, activityId);
        });
    }

    @Override
    public List<Activity> getActivitiesByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_BY_PRACTITIONER, this::mapResultSetToActivity, practitionerId);
    }

    @Override
    public List<Activity> getActivitiesByReport(int reportId) throws DAOException {
        return recoverALL(SQL_SELECT_BY_REPORT, this::mapResultSetToActivity, reportId);
    }

    @Override
    public boolean assignActivityToReport(int activityId, int reportId) throws DAOException {
        return updateTuple(SQL_ASSIGN_REPORT, statement -> {
            statement.setInt(1, reportId);
            statement.setInt(2, activityId);
        });
    }

    @Override
    public boolean removeActivityFromReport(int activityId) throws DAOException {
        return updateTuple(SQL_REMOVE_REPORT, statement -> {
            statement.setInt(1, activityId);
        });
    }

    private Activity mapResultSetToActivity(ResultSet resultSet) throws SQLException {
        Activity activity = new Activity();
        activity.setActivityId(resultSet.getInt("activity_id"));
        activity.setPractitionerId(resultSet.getInt("practitioner_id"));

        int reportId = resultSet.getInt("report_id");
        activity.setReportId(resultSet.wasNull() ? null : reportId);

        activity.setTitle(resultSet.getString("title"));
        activity.setDescription(resultSet.getString("description"));
        activity.setActivityDate(resultSet.getDate("activity_date"));
        activity.setDurationHours(resultSet.getInt("duration_hours"));

        return activity;
    }
}