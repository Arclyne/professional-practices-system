package mx.uv.fei.dataacces.repositories;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;


import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.domain.dto.Activity;


public class ActivityDAO implements IActivityDAO {
    private static final String SQL_INSERT = "INSERT INTO ACTIVIDAD (NOMBRE, FECHA_INICIO, FECHA_END, DESCRIPCION, ENCARGADO) VALUES (?, ?, ?, ?, ?)";

    public boolean insertActivity(Activity activity) throws DAOException {
        try (
            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(SQL_INSERT)
        ) {
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
}
