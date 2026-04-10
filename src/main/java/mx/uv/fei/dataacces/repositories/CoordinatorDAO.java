package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataacces.exceptions.DAOException;

public class CoordinatorDAO {
    private final UserDAO userDAO = new UserDAO();

    public boolean insertCoordinator(Coordinator coordinator) throws DAOException {
        boolean isSuccess = false;

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            try {
                int userId = userDAO.insertUser(coordinator, connection);
                
                if (userId == -1) {
                    throw new SQLException("Falló la inserción en la tabla base Usuario.");
                }

                String query = "INSERT INTO COORDINADOR (ID_COORDINADOR) VALUES (?)";
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, userId);
                    isSuccess = statement.executeUpdate() > 0;
                }

                connection.commit();
                
                coordinator.setId(userId); 

            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("Error al insertar coordinador, se revirtieron los cambios.", e);
            } finally {
                connection.setAutoCommit(true);
            }
            
        } catch (SQLException e) {
            throw new DAOException("Error de conexión a la base de datos.", e);
        }

        return isSuccess;
    }
}