package mx.uv.fei.dataacces.repositories;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;


import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;
import mx.uv.fei.domain.dto.Coordinator;


public class CoordinatorDAO implements ICoordinatorDAO {
    private final UserDAO userDAO = new UserDAO();

    public boolean insertCoordinador(Coordinator coordinator) throws DAOException {
        int userId = userDAO.insertUser(coordinator);

        if (userId == -1) {
            throw new DAOException("Falló la inserción en la tabla base Usuario.", null);
        }

        String query = "INSERT INTO COORDINADOR (ID_COORDINADOR, FECHA_BAJA) VALUES (?, ?)";

        try (
                Connection connection = DatabaseConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
            ) {
            statement.setInt(1, userId);

            if (coordinator.getDischargeDate() != null) {
                statement.setTimestamp(2, Timestamp.valueOf(coordinator.getDischargeDate()));
            } else {
                statement.setNull(2, java.sql.Types.TIMESTAMP);
            }

            coordinator.setId(userId);

            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DAOException("Error al insertar Coordinador: ", e);
        }
    }
}