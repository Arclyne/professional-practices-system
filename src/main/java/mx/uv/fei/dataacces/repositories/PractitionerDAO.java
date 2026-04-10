package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.dto.Practitioner;

public class PractitionerDAO implements IPractitionerDAO {

    @Override
    public int insertPractitioner(Practitioner practitioner) throws DAOException {
        int resultId = -1;
        UserDAO userDAO = new UserDAO();

        try (Connection connection = DatabaseConnection.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            try {
                int generatedUserId = userDAO.insertUser(practitioner, connection);

                if (generatedUserId > 0) {
                    String query = "INSERT INTO PRACTICANTE (ID_PRACTICANTE, LENGUA_INDIGENA, CALIFICACION) VALUES (?, ?, ?)";

                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, generatedUserId);
                        statement.setString(2, practitioner.getIndigenousLanguage());
                        statement.setDouble(3, practitioner.getGrade());

                        int affectedRows = statement.executeUpdate();

                        if (affectedRows > 0) {
                            resultId = generatedUserId;
                        }
                    }
                    
                    connection.commit();
                    
                } else {
                    connection.rollback();
                }

            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("SQL Error while inserting practitioner. Changes were rolled back.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Critical database connection error.", e);
        }

        return resultId;
    }
}