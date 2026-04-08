package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IPracticanteDAO;
import mx.uv.fei.domain.dto.Practicante;

public class PracticanteDAO implements IPracticanteDAO {

    @Override
    public int insertPracticante(Practicante practicante) throws DAOException {
        int resultId = -1;
        
        UserDAO userDAO = new UserDAO();

        int generatedUserId = userDAO.insertUser(practicante);

        if (generatedUserId > 0) {
            String query = "INSERT INTO PRACTICANTE (ID_PRACTICANTE, LENGUA_INDIGENA, CALIFICACION) VALUES (?, ?, ?)";

            try (
                Connection connection = DatabaseConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
            ) {
                statement.setInt(1, generatedUserId);
                statement.setString(2, practicante.getLenguaIndigena());
                statement.setInt(3, practicante.getCalificacion());

                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    resultId = generatedUserId;
                }
            } catch (SQLException e) {
                throw new DAOException("Error al intentar insertar el practicante en la base de datos.", e);
            }
        }

        return resultId;
    }
}