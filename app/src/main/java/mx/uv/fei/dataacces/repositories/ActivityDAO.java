package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.sql.ResultSet;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;

@Component
public class ActivityDAO extends BaseDAO implements IActivityDAO {

    @Inject
    public ActivityDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    private static final String SQL_INSERT = "INSERT INTO activity (NAME, START_DATE, END_DATE, DESCRIPTION, MANAGER) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECTTOSEARCH = "SELECT ID_ACTIVITY, NAME, START_DATE, END_DATE, DESCRIPTION, MANAGER FROM activity WHERE NAME = ? AND MANAGER = ?";
    private static final String SQL_SELECTALL = "SELECT * FROM activity";
    private static final String SQL_UPDATE = "UPDATE activity SET NAME = ?, START_DATE = ?, END_DATE = ?, DESCRIPTION = ?, MANAGER = ? WHERE ID_ACTIVITY = ?";

    @Override
    public boolean insertActivity(Activity activity) throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setString(1, activity.getName());
            statement.setDate(2, activity.getStartDate());
            statement.setDate(3, activity.getEndDate());
            statement.setString(4, activity.getDescription());
            statement.setString(5, activity.getManager());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la actividad en la base de datos.", e);
        }
    }

    @Override
    public Activity recoverActivity(String activityName, String manager) throws DAOException {
        Activity activityToSearch = new Activity();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECTTOSEARCH)) {
            statement.setString(1, activityName);
            statement.setString(2, manager);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    mapActivity(activityToSearch, resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar la actividad de la base de datos.", e);
        }
        return activityToSearch;
    }

    private void mapActivity(Activity activityToSearch, ResultSet resultSet) throws SQLException {
        activityToSearch.setActivityId(resultSet.getInt("ID_ACTIVITY"));
        activityToSearch.setName(resultSet.getString("NAME"));
        activityToSearch.setStartDate(resultSet.getDate("START_DATE"));
        activityToSearch.setEndDate(resultSet.getDate("END_DATE"));
        activityToSearch.setDescription(resultSet.getString("DESCRIPTION"));
        activityToSearch.setManager(resultSet.getString("MANAGER"));
    }

    @Override
    public List<Activity> getAllActivity() throws DAOException {
        return recoverALL(SQL_SELECTALL,
                resultSet -> {
                    Activity activityRecovered = new Activity();
                    mapActivity(activityRecovered, resultSet);
                    return activityRecovered;
                });
    }

    @Override
    public boolean updateActivity(Activity activity, int ID) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, activity.getName());
            statement.setObject(2, activity.getStartDate());
            statement.setObject(3, activity.getEndDate());
            statement.setString(4, activity.getDescription());
            statement.setString(5, activity.getManager());
            statement.setInt(6, ID);
        });
    }
}