package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProfessorDAO;
import mx.uv.fei.domain.dto.Professor;

@Repository
public class ProfessorDAO implements IProfessorDAO {

    private final IDatabaseConnection dbConnection;
    private final UserDAO userDAO;

    @Autowired
    public ProfessorDAO(IDatabaseConnection dbConnection, UserDAO userDAO) {
        this.dbConnection = dbConnection;
        this.userDAO = userDAO;
    }

    @Override
    public int insertProfessor(Professor professor) throws DAOException {
        int resultId = -1;

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int generatedUserId = userDAO.insertUser(professor, connection);

                if (generatedUserId > 0) {
                    String query = "INSERT INTO PROFESOR (ID_PROFESOR) VALUES (?)";

                    try (PreparedStatement statement = connection.prepareStatement(query)) {
                        statement.setInt(1, generatedUserId);

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
                throw new DAOException("SQL Error while inserting professor. Changes were rolled back.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Critical database connection error.", e);
        }

        return resultId;
    }
}