package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.dto.Practitioner;

@Repository
public class PractitionerDAO extends BaseDAO implements IPractitionerDAO {
    private final UserDAO userDAO;

    @Autowired
    public PractitionerDAO(IDatabaseConnection dbconnection, UserDAO userDAO) {
        super(dbconnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertPractitioner(Practitioner practitioner) throws DAOException {
        int resultId = -1;

        try (Connection connection = dbConnection.getConnection()) {
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

    @Override
    public Practitioner recoverPractitioner(int practitionerId) throws DAOException {
        Practitioner practitioner = null;
        
        return practitioner;
    }
}