package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProfessorDAO;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class ProfessorDAO extends BaseDAO implements IProfessorDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO professor (ID_PROFESSOR) VALUES (?)";

    private static final String SQL_SELECT_ONE = "SELECT u.ID_USER, u.USERNAME, u.PASSWORD, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.ROLE_NAME, u.STATUS, u.GENDER, u.REGISTRATION_DATE, u.TERMINATION_DATE FROM professor p INNER JOIN user u ON p.ID_PROFESSOR = u.ID_USER WHERE p.ID_PROFESSOR = ?";
    private static final String SQL_SELECT_ALL = "SELECT u.ID_USER, u.USERNAME, u.PASSWORD, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.ROLE_NAME, u.STATUS, u.GENDER, u.REGISTRATION_DATE, u.TERMINATION_DATE FROM professor p INNER JOIN user u ON p.ID_PROFESSOR = u.ID_USER";

    @Inject
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
        try (Connection connection = databaseConnection.getConnection();
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
        professor.setId(resultSet.getInt("ID_USER"));
        professor.setUserName(resultSet.getString("USERNAME"));
        professor.setPassword(resultSet.getString("PASSWORD"));
        professor.setName(resultSet.getString("FIRST_NAME"));
        professor.setLastName(resultSet.getString("LAST_NAME"));
        professor.setEmail(resultSet.getString("EMAIL"));
        professor.setRole(resultSet.getString("ROLE_NAME"));

        String statusValue = resultSet.getString("STATUS");
        professor.setStatus(statusValue != null ? UserStatus.fromString(statusValue) : null);

        String genderValue = resultSet.getString("GENDER");
        professor.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

        if (resultSet.getTimestamp("REGISTRATION_DATE") != null) {
            professor.setRegistrationDate(resultSet.getTimestamp("REGISTRATION_DATE").toLocalDateTime());
        }
        if (resultSet.getTimestamp("TERMINATION_DATE") != null) {
            professor.setDischargeDate(resultSet.getTimestamp("TERMINATION_DATE").toLocalDateTime());
        }
    }
}