package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProfessorDAO;
import mx.uv.fei.domain.dto.Professor;

@Repository
public class ProfessorDAO extends BaseDAO implements IProfessorDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO PROFESOR (ID_PROFESOR) VALUES (?)";
    private static final String SQL_SELECT_ONE = "SELECT U.ID_USUARIO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.ESTADO, U.GENERO, P.FECHA_REGISTRO, P.FECHA_BAJA FROM PROFESOR P INNER JOIN USUARIO U ON P.ID_PROFESOR = U.ID_USUARIO WHERE P.ID_PROFESOR = ?";
    private static final String SQL_SELECT_ALL = "SELECT U.ID_USUARIO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.ESTADO, U.GENERO, P.FECHA_REGISTRO, P.FECHA_BAJA FROM PROFESOR P INNER JOIN USUARIO U ON P.ID_PROFESOR = U.ID_USUARIO";

    @Autowired
    public ProfessorDAO(IDatabaseConnection dbConnection, UserDAO userDAO) {
        super(dbConnection);
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
                    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
                        statement.setInt(1, generatedUserId);

                        if (statement.executeUpdate() > 0) {
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

    @Override
    public Professor recoverProfessor(int professorId) throws DAOException {
        Professor professorToSearch = new Professor();
        
        try (
                Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {
            
            statement.setInt(1, professorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    professorToSearch.setId(resultSet.getInt("ID_USUARIO"));
                    professorToSearch.setPassword(resultSet.getString("PASSWORD"));
                    professorToSearch.setName(resultSet.getString("NOMBRE"));
                    professorToSearch.setLastName(resultSet.getString("APELLIDOS"));
                    professorToSearch.setStatus(resultSet.getString("ESTADO"));
                    professorToSearch.setGender(resultSet.getString("GENERO"));
                    
                    professorToSearch.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
                    
                    if (resultSet.getTimestamp("FECHA_BAJA") != null) {
                        professorToSearch.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el profesor de la base de datos.", e);
        }
        return professorToSearch;
    }

    @Override
    public List<Professor> getAllProfessors() throws DAOException {
        return recoverALL(SQL_SELECT_ALL, resultSet -> {
            Professor professorRecovered = new Professor();
            
            professorRecovered.setId(resultSet.getInt("ID_USUARIO"));
            professorRecovered.setPassword(resultSet.getString("PASSWORD"));
            professorRecovered.setName(resultSet.getString("NOMBRE"));
            professorRecovered.setLastName(resultSet.getString("APELLIDOS"));
            professorRecovered.setStatus(resultSet.getString("ESTADO"));
            professorRecovered.setGender(resultSet.getString("GENERO"));
            
            professorRecovered.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
            if (resultSet.getTimestamp("FECHA_BAJA") != null) {
                professorRecovered.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
            }

            return professorRecovered;
        });
    }

    @Override
    public boolean updateProfessor(Professor professorToUpdate, int id) throws DAOException {
        boolean isUpdated = false;
        professorToUpdate.setId(id);

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                isUpdated = userDAO.updateUser(professorToUpdate, connection);

                if (isUpdated) {
                    connection.commit();
                } else {
                    connection.rollback();
                }

            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("SQL Error while updating professor. Changes were rolled back.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Critical database connection error.", e);
        }

        return isUpdated;
    }
}