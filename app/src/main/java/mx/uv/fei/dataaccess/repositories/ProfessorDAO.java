package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProfessorDAO;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class ProfessorDAO extends BaseDAO implements IProfessorDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO professor (professor_id) VALUES (?)";

    private static final String SQL_SELECT_ONE = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date FROM professor p INNER JOIN user u ON p.professor_id = u.user_id WHERE p.professor_id = ?";
    private static final String SQL_SELECT_ALL = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date FROM professor p INNER JOIN user u ON p.professor_id = u.user_id";

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
        professor.setId(resultSet.getInt("user_id"));
        professor.setUserName(resultSet.getString("username"));
        professor.setPassword(resultSet.getString("password"));
        professor.setName(resultSet.getString("name"));
        professor.setLastName(resultSet.getString("last_name"));
        professor.setEmail(resultSet.getString("email"));
        professor.setRole(resultSet.getString("role_name"));

        String statusValue = resultSet.getString("status");
        professor.setStatus(statusValue != null ? UserStatus.fromString(statusValue) : null);

        String genderValue = resultSet.getString("gender");
        professor.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

        if (resultSet.getTimestamp("registration_date") != null) {
            professor.setRegistrationDate(resultSet.getTimestamp("registration_date").toLocalDateTime());
        }
        if (resultSet.getTimestamp("discharge_date") != null) {
            professor.setDischargeDate(resultSet.getTimestamp("discharge_date").toLocalDateTime());
        }
    }
}