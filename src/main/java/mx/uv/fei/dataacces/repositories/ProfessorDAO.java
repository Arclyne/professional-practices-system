package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProfessorDAO;
import mx.uv.fei.domain.dto.Professor;

public class ProfessorDAO extends BaseDAO implements IProfessorDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO PROFESOR (ID_PROFESOR) VALUES (?)";

    private static final String SQL_SELECT_ONE = "SELECT U.ID_USUARIO, U.USER, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.NOMBRE_ROL, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA FROM PROFESOR P INNER JOIN USUARIO U ON P.ID_PROFESOR = U.ID_USUARIO WHERE P.ID_PROFESOR = ?";
    private static final String SQL_SELECT_ALL = "SELECT U.ID_USUARIO, U.USER, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.NOMBRE_ROL, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA FROM PROFESOR P INNER JOIN USUARIO U ON P.ID_PROFESOR = U.ID_USUARIO";

    public ProfessorDAO(IDatabaseConnection databaseConnection, UserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertProfessor(Professor professor) throws DAOException {
        int resultId = -1;

        try (Connection connection = databaseConnection.getConnection()) {
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
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {

            statement.setInt(1, professorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    mapProfessor(professorToSearch, resultSet);
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
            mapProfessor(professorRecovered, resultSet);
            return professorRecovered;
        });
    }

    @Override
    public boolean updateProfessor(Professor professorToUpdate, int id) throws DAOException {
        professorToUpdate.setId(id);

        try (Connection connection = databaseConnection.getConnection()) {
            return userDAO.updateUser(professorToUpdate, connection);
        } catch (SQLException e) {
            throw new DAOException("Critical database connection error during professor update.", e);
        }
    }

    private void mapProfessor(Professor professor, ResultSet resultSet) throws SQLException {
        professor.setId(resultSet.getInt("ID_USUARIO"));
        professor.setUserName(resultSet.getString("USER")); // El antiguo staffNumber ahora es userName
        professor.setPassword(resultSet.getString("PASSWORD"));
        professor.setName(resultSet.getString("NOMBRE"));
        professor.setLastName(resultSet.getString("APELLIDOS"));
        professor.setEmail(resultSet.getString("CORREO"));
        professor.setRole(resultSet.getString("NOMBRE_ROL"));
        professor.setStatus(resultSet.getString("ESTADO"));
        professor.setGender(resultSet.getString("GENERO"));

        if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
            professor.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
        }
        if (resultSet.getTimestamp("FECHA_BAJA") != null) {
            professor.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
        }
    }
}