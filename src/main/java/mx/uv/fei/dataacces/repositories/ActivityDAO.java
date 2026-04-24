package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.sql.ResultSet;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;

public class ActivityDAO extends BaseDAO implements IActivityDAO {

    public ActivityDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    private static final String SQL_INSERT = "INSERT INTO ACTIVIDAD (NOMBRE, FECHA_INICIO, FECHA_END, DESCRIPCION, ENCARGADO) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECTTOSEARCH = "SELECT ID_ACTIVIDAD, NOMBRE, FECHA_INICIO, FECHA_END, DESCRIPCION, ENCARGADO FROM ACTIVIDAD WHERE NOMBRE = ? AND ENCARGADO = ?";
    private static final String SQL_SELECTALL = "SELECT * FROM ACTIVIDAD";
    private static final String SQL_UPDATE = "UPDATE ACTIVIDAD SET NOMBRE = ? , FECHA_INICIO = ?, FECHA_END = ?, DESCRIPCION = ?, ENCARGADO = ? WHERE ID_ACTIVIDAD =?";

    @Override
    public boolean insertActivity(Activity activity) throws DAOException {

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
            statement.setString(1, activity.getName());
            statement.setDate(2, activity.getStartDate());
            statement.setDate(3, activity.getEndDate());
            statement.setString(4, activity.getDescription());
            statement.setString(5, activity.getManager());

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar el usuario en la base de datos.", e);
        }
    }

    @Override
    public Activity recoverActivity(String activityName, String manager) throws DAOException {

        Activity activityToSearch = new Activity();
        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECTTOSEARCH);) {
            statement.setString(1, activityName);
            statement.setString(2, manager);

            try (
                    ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    activityToSearch.setActivityId(resultSet.getInt("ID_ACTIVIDAD"));
                    activityToSearch.setName(resultSet.getString("NOMBRE"));
                    activityToSearch.setStartDate(resultSet.getDate("FECHA_INICIO"));
                    activityToSearch.setEndDate(resultSet.getDate("FECHA_END"));
                    activityToSearch.setDescription(resultSet.getString("DESCRIPCION"));
                    activityToSearch.setManager(resultSet.getString("ENCARGADO"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar la organización en la base de datos.", e);
        }
        return activityToSearch;
    }

    @Override
    public List<Activity> getAllActivity() throws DAOException {

        return recoverALL(SQL_SELECTALL,
                resultSet -> {
                    Activity activityRecovered = new Activity();
                    activityRecovered.setActivityId(resultSet.getInt("ID_ACTIVIDAD"));
                    activityRecovered.setName(resultSet.getString("NOMBRE"));
                    activityRecovered.setStartDate(resultSet.getDate("FECHA_INICIO"));
                    activityRecovered.setEndDate(resultSet.getDate("FECHA_END"));
                    activityRecovered.setDescription(resultSet.getString("DESCRIPCION"));
                    activityRecovered.setManager(resultSet.getString("ENCARGADO"));

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
