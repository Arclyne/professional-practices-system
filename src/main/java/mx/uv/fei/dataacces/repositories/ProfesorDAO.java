package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IProfesorDAO;
import mx.uv.fei.domain.dto.Profesor;

public class ProfesorDAO implements IProfesorDAO {

    public int insertProfesor(Profesor profesor) throws DAOException {
        int resultId = -1;
        
        UserDAO userDAO = new UserDAO();

        int generatedUserId = userDAO.insertUser(profesor);

        if (generatedUserId > 0) {
            String query = "INSERT INTO PROFESOR (ID_PROFESOR, FECHA_REGISTRO) VALUES (?, ?)";

            try (
                Connection connection = DatabaseConnection.getInstance().getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
            ) {
                statement.setInt(1, generatedUserId);
    
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));

                int affectedRows = statement.executeUpdate();

                if (affectedRows > 0) {
                    resultId = generatedUserId;
                }
            } catch (SQLException e) {
                throw new DAOException("Error al intentar insertar el profesor en la base de datos.", e);
            }
        }

        return resultId;
    }
}