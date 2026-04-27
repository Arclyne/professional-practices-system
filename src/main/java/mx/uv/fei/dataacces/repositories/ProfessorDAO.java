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

    private static final String SQL_INSERT = "INSERT INTO PROFESOR (ID_PROFESOR, NUMERO_PERSONAL) VALUES (?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT U.ID_USUARIO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA, P.NUMERO_PERSONAL FROM PROFESOR P INNER JOIN USUARIO U ON P.ID_PROFESOR = U.ID_USUARIO WHERE P.ID_PROFESOR = ?";
    private static final String SQL_SELECT_ALL = "SELECT U.ID_USUARIO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA, P.NUMERO_PERSONAL FROM PROFESOR P INNER JOIN USUARIO U ON P.ID_PROFESOR = U.ID_USUARIO";
    private static final String SQL_UPDATE_PROFESSOR = "UPDATE PROFESOR SET NUMERO_PERSONAL = ? WHERE ID_PROFESOR = ?";

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
                        statement.setString(2, professor.getStaffNumber());

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
                    professorToSearch.setId(resultSet.getInt("ID_USUARIO"));
                    professorToSearch.setPassword(resultSet.getString("PASSWORD"));
                    professorToSearch.setName(resultSet.getString("NOMBRE"));
                    professorToSearch.setLastName(resultSet.getString("APELLIDOS"));
                    professorToSearch.setEmail(resultSet.getString("CORREO")); // Recuperamos Correo
                    professorToSearch.setStatus(resultSet.getString("ESTADO"));
                    professorToSearch.setGender(resultSet.getString("GENERO"));

                    professorToSearch.setStaffNumber(resultSet.getString("NUMERO_PERSONAL")); // Recuperamos No. Personal

                    if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
                        professorToSearch.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
                    }

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
            professorRecovered.setEmail(resultSet.getString("CORREO"));
            professorRecovered.setStatus(resultSet.getString("ESTADO"));
            professorRecovered.setGender(resultSet.getString("GENERO"));

            professorRecovered.setStaffNumber(resultSet.getString("NUMERO_PERSONAL")); // Recuperamos No. Personal

            if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
                professorRecovered.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
            }
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

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean userUpdated = userDAO.updateUser(professorToUpdate, connection);

                if (userUpdated) {
                    try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PROFESSOR)) {
                        statement.setString(1, professorToUpdate.getStaffNumber());
                        statement.setInt(2, id);

                        if (statement.executeUpdate() >= 0) {
                            isUpdated = true;
                        }
                    }

                    if (isUpdated) {
                        connection.commit();
                    } else {
                        connection.rollback();
                    }
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