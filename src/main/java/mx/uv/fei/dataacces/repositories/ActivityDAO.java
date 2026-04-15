package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.sql.ResultSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;

@Repository
public class ActivityDAO extends BaseDAO implements IActivityDAO {

    @Autowired
    public ActivityDAO(IDatabaseConnection dbConnection) {
        super(dbConnection);
    }

    private static final String SQL_INSERT = "INSERT INTO ACTIVIDAD (NOMBRE, FECHA_INICIO, FECHA_END, DESCRIPCION, ENCARGADO) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECTTOSEARCH = "SELECT ID_ACTIVIDAD, NOMBRE, ENCARGADO FROM ACTIVIDAD WHERE NOMBRE = ? AND ENCARGADO = ?";
    private static final String SQL_SELECTALL = "SELECT * FROM ACTIVIDAD";

    @Override
    public boolean insertActivity(Activity activity) throws DAOException {
        try (
                Connection connection = dbconnection.getConnection();
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
                Connection connection = dbconnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECTTOSEARCH);) {
            statement.setString(1, activityName);
            statement.setString(2, manager);

            try (
                    ResultSet resultset = statement.executeQuery()) {
                if (resultset.next()) {
                    activityToSearch.setName(resultset.getString("NOMBRE"));
                    activityToSearch.setActivityId(resultset.getInt("ID_ACTIVIDAD"));
                    activityToSearch.setManager(resultset.getString("ENCARGADO"));
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
                rs -> {
                    Activity activity = new Activity();
                    activity.setActivityId(rs.getInt("ID_ACTIVIDAD"));
                    activity.setName(rs.getString("NOMBRE"));
                    activity.setStartDate(rs.getDate("FECHA_INICIO"));
                    activity.setEndDate(rs.getDate("FECHA_END"));
                    activity.setDescription(rs.getString("DESCRIPCION"));
                    activity.setManager(rs.getString("ENCARGADO"));

                    return activity;
                });
    }
}
