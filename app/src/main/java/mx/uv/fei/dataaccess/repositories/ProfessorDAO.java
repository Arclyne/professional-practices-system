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
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class ProfessorDAO extends BaseDAO implements IProfessorDAO {

    private static final String SQL_INSERT_PROFESSOR = "INSERT INTO professor (professor_id) VALUES (?)";
    private static final String SQL_SELECT_PROFESSOR_BY_ID = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date FROM professor p INNER JOIN user u ON p.professor_id = u.user_id WHERE p.professor_id = ?";
    private static final String SQL_SELECT_ALL_PROFESSORS = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date FROM professor p INNER JOIN user u ON p.professor_id = u.user_id";

    private final IUserDAO userDAO;

    @Inject
    public ProfessorDAO(IDatabaseConnection databaseConnection, IUserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertProfessor(Professor professor) throws DAOException {
        int resultId = -1;

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                resultId = executeProfessorInsertTransaction(connection, professor);

                if (resultId > 0) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException | DAOException e) {
                connection.rollback();
                throw new DAOException("Error SQL al insertar el profesor. Los cambios fueron revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }

        return resultId;
    }

    private int executeProfessorInsertTransaction(Connection connection, Professor professor) throws SQLException, DAOException {
        int insertedProfessorId = -1;
        int generatedUserId = userDAO.insertUser(professor, connection);

        if (generatedUserId > 0) {
            try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PROFESSOR)) {
                statement.setInt(1, generatedUserId);

                if (statement.executeUpdate() > 0) {
                    insertedProfessorId = generatedUserId;
                }
            }
        }

        return insertedProfessorId;
    }

    @Override
    public Professor recoverProfessor(int professorId) throws DAOException {
        Professor recoveredProfessor = new Professor();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_PROFESSOR_BY_ID)) {

            statement.setInt(1, professorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredProfessor = mapResultSetToProfessor(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el profesor de la base de datos.", e);
        }

        return recoveredProfessor;
    }

    @Override
    public List<Professor> getAllProfessors() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_PROFESSORS, this::mapResultSetToProfessor);
    }

    @Override
    public boolean updateProfessor(Professor professorToUpdate, int professorId) throws DAOException {
        boolean isUpdated = false;
        professorToUpdate.setId(professorId);

        try (Connection connection = databaseConnection.getConnection()) {
            isUpdated = userDAO.updateUser(professorToUpdate, connection);
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión al actualizar el profesor.", e);
        }

        return isUpdated;
    }

    private Professor mapResultSetToProfessor(ResultSet resultSet) throws SQLException {
        Professor professor = new Professor();

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

        return professor;
    }
}