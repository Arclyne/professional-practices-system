package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.sql.ResultSet;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;

@Component
public class ActivityDAO extends BaseDAO implements IActivityDAO {

    private static final String SQL_INSERT_ACTIVITY = "INSERT INTO activity (name, start_date, end_date, description, manager) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ACTIVITY_BY_NAME = "SELECT activity_id, name, start_date, end_date, description, manager FROM activity WHERE name = ? AND manager = ?";
    private static final String SQL_SELECT_ALL_ACTIVITIES = "SELECT activity_id, name, start_date, end_date, description, manager FROM activity";
    private static final String SQL_UPDATE_ACTIVITY = "UPDATE activity SET name = ?, start_date = ?, end_date = ?, description = ?, manager = ? WHERE activity_id = ?";

    @Inject
    public ActivityDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public boolean insertActivity(Activity activity) throws DAOException {
        boolean isInserted;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_ACTIVITY)) {

            statement.setString(1, activity.getName());
            statement.setDate(2, activity.getStartDate());
            statement.setDate(3, activity.getEndDate());
            statement.setString(4, activity.getDescription());
            statement.setString(5, activity.getManager());

            isInserted = statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la actividad en la base de datos.", e);
        }

        return isInserted;
    }

    @Override
    public Activity recoverActivity(String activityName, String manager) throws DAOException {
        Activity recoveredActivity = new Activity();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ACTIVITY_BY_NAME)) {

            statement.setString(1, activityName);
            statement.setString(2, manager);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredActivity = mapResultSetToActivity(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar la actividad de la base de datos.", e);
        }

        return recoveredActivity;
    }

    private Activity mapResultSetToActivity(ResultSet resultSet) throws SQLException {
        Activity activity = new Activity();

        activity.setActivityId(resultSet.getInt("activity_id"));
        activity.setName(resultSet.getString("name"));
        activity.setStartDate(resultSet.getDate("start_date"));
        activity.setEndDate(resultSet.getDate("end_date"));
        activity.setDescription(resultSet.getString("description"));
        activity.setManager(resultSet.getString("manager"));

        return activity;
    }

    @Override
    public List<Activity> getAllActivities() throws DAOException {
        List<Activity> activitiesList;

        activitiesList = recoverALL(SQL_SELECT_ALL_ACTIVITIES, this::mapResultSetToActivity);

        return activitiesList;
    }

    @Override
    public boolean updateActivity(Activity activity, int activityId) throws DAOException {
        boolean isUpdated;

        isUpdated = updateTuple(SQL_UPDATE_ACTIVITY, statement -> {
            statement.setString(1, activity.getName());
            statement.setObject(2, activity.getStartDate());
            statement.setObject(3, activity.getEndDate());
            statement.setString(4, activity.getDescription());
            statement.setString(5, activity.getManager());
            statement.setInt(6, activityId);
        });

        return isUpdated;
    }
}