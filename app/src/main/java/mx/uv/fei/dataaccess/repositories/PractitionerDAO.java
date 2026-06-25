package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso a datos de los practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class PractitionerDAO extends BaseDAO implements IPractitionerDAO {

    private static final String CURRENT_GROUP_SUBQUERY =
            "(SELECT ge.group_id FROM group_enrollment ge WHERE ge.practitioner_id = p.practitioner_id " +
                    "ORDER BY ge.enrollment_id DESC LIMIT 1) AS group_id ";
    private static final String SQL_INSERT_PRACTITIONER =
            "INSERT INTO practitioner (practitioner_id, indigenous_language, grade) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_PRACTITIONER =
            "UPDATE practitioner SET indigenous_language = ?, grade = ? WHERE practitioner_id = ?";
    private static final String SQL_SELECT_PRACTITIONER_BY_ID =
            "SELECT u.user_id, u.username AS matricula, u.password, u.name, u.last_name, " +
                    "u.email, u.status, u.gender, p.indigenous_language, p.grade, " +
                    CURRENT_GROUP_SUBQUERY +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "WHERE p.practitioner_id = ?";
    private static final String SQL_SELECT_ALL_PRACTITIONERS =
            "SELECT u.user_id, u.username AS matricula, u.password, u.name, u.last_name, " +
                    "u.email, u.status, u.gender, p.indigenous_language, p.grade, " +
                    CURRENT_GROUP_SUBQUERY +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id";
    private static final String SQL_SELECT_PRACTITIONERS_PENDING_ASSIGNMENT =
            "SELECT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "WHERE p.practitioner_id IN (SELECT practitioner_id FROM project_postulation) " +
                    "AND p.practitioner_id NOT IN " +
                    "(SELECT practitioner_id FROM project_postulation WHERE postulation_status = 'Assigned')";
    private static final String SQL_SELECT_ASSIGNED_PRACTITIONERS =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN project_postulation pp ON p.practitioner_id = pp.practitioner_id " +
                    "WHERE u.status = 'Active' AND pp.postulation_status = 'Assigned'";
    private static final String SQL_SELECT_PRACTITIONERS_BY_PROFESSOR =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN project_postulation pp ON p.practitioner_id = pp.practitioner_id " +
                    "INNER JOIN group_enrollment ge ON p.practitioner_id = ge.practitioner_id " +
                    "INNER JOIN practice_group pg ON ge.group_id = pg.group_id " +
                    "WHERE u.status = 'Active' " +
                    "AND pp.postulation_status = 'Assigned' " +
                    "AND pg.professor_id = ?";
    private static final String SQL_SELECT_PRACTITIONERS_BY_PROFESSOR_AND_PERIOD =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN project_postulation pp ON p.practitioner_id = pp.practitioner_id " +
                    "INNER JOIN group_enrollment ge ON p.practitioner_id = ge.practitioner_id " +
                    "INNER JOIN practice_group pg ON ge.group_id = pg.group_id " +
                    "WHERE u.status = 'Active' " +
                    "AND pp.postulation_status = 'Assigned' " +
                    "AND pg.professor_id = ? " +
                    "AND pg.period_id = ?";
    private static final String SQL_SELECT_PRACTITIONERS_BY_GROUP =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN project_postulation pp ON p.practitioner_id = pp.practitioner_id " +
                    "INNER JOIN group_enrollment ge ON p.practitioner_id = ge.practitioner_id " +
                    "WHERE u.status = 'Active' " +
                    "AND pp.postulation_status = 'Assigned' " +
                    "AND ge.group_id = ?";
    private static final String SQL_SELECT_ENROLLED_PRACTITIONERS_BY_GROUP =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN group_enrollment ge ON p.practitioner_id = ge.practitioner_id " +
                    "WHERE u.status = 'Active' " +
                    "AND ge.status = 'Active' " +
                    "AND ge.group_id = ?";

    private final IUserDAO userDAO;

    @Inject
    public PractitionerDAO(IDatabaseConnection databaseConnection, IUserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertPractitioner(Practitioner practitioner) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int generatedUserId = userDAO.insertUser(practitioner, connection);

                if (generatedUserId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PRACTITIONER)) {
                        statement.setInt(1, generatedUserId);
                        statement.setString(2, practitioner.getIndigenousLanguage());
                        statement.setDouble(3, practitioner.getGrade());

                        if (statement.executeUpdate() > 0) {
                            generatedId = generatedUserId;
                        }
                    }
                }

                if (generatedId > 0) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException | DAOException e) {
                connection.rollback();
                throw new DAOException("Error SQL al insertar el practicante. Cambios revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }

        return generatedId;
    }

    @Override
    public Practitioner recoverPractitioner(int practitionerId) throws DAOException {
        Practitioner recoveredPractitioner = new Practitioner();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_PRACTITIONER_BY_ID)) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredPractitioner = mapResultSetToPractitioner(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error en la base de datos al intentar recuperar el practicante.", e);
        }

        return recoveredPractitioner;
    }

    @Override
    public List<Practitioner> getAllPractitioners() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_PRACTITIONERS, this::mapResultSetToPractitioner);
    }

    @Override
    public void updatePractitioner(Practitioner practitionerToUpdate, int practitionerId) throws DAOException {
        practitionerToUpdate.setId(practitionerId);

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                executeUpdateTransaction(connection, practitionerToUpdate, practitionerId);
                connection.commit();
            } catch (SQLException | DAOException e) {
                connection.rollback();
                throw new DAOException("Error SQL al actualizar el practicante. Cambios revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }
    }

    @Override
    public List<Practitioner> retrievePractitionersPendingAssignment() throws DAOException {
        return recoverALL(SQL_SELECT_PRACTITIONERS_PENDING_ASSIGNMENT, this::mapResultSetToMinimalPractitioner);
    }

    @Override
    public List<Practitioner> retrieveAssignedPractitioners() throws DAOException {
        return recoverALL(SQL_SELECT_ASSIGNED_PRACTITIONERS, this::mapResultSetToMinimalPractitioner);
    }

    @Override
    public List<Practitioner> retrievePractitionersByProfessor(int professorId) throws DAOException {
        return recoverALL(SQL_SELECT_PRACTITIONERS_BY_PROFESSOR, this::mapResultSetToMinimalPractitioner, professorId);
    }

    @Override
    public List<Practitioner> retrievePractitionersByProfessorAndPeriod(int professorId, int periodId)
            throws DAOException {
        return recoverALL(SQL_SELECT_PRACTITIONERS_BY_PROFESSOR_AND_PERIOD, this::mapResultSetToMinimalPractitioner,
                professorId, periodId);
    }

    @Override
    public List<Practitioner> retrievePractitionersByGroup(int groupId) throws DAOException {
        return recoverALL(SQL_SELECT_PRACTITIONERS_BY_GROUP, this::mapResultSetToMinimalPractitioner, groupId);
    }

    @Override
    public List<Practitioner> retrieveEnrolledPractitionersByGroup(int groupId) throws DAOException {
        return recoverALL(SQL_SELECT_ENROLLED_PRACTITIONERS_BY_GROUP, this::mapResultSetToMinimalPractitioner, groupId);
    }

    private void executeUpdateTransaction(Connection connection, Practitioner practitioner, int practitionerId)
            throws SQLException, DAOException {
        userDAO.updateUser(practitioner, connection);

        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PRACTITIONER)) {
            statement.setString(1, practitioner.getIndigenousLanguage());
            statement.setDouble(2, practitioner.getGrade());
            statement.setInt(3, practitionerId);
            statement.executeUpdate();
        }
    }

    private Practitioner mapResultSetToMinimalPractitioner(ResultSet resultSet) throws SQLException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(resultSet.getInt("user_id"));
        practitioner.setEnrollment(resultSet.getString("matricula"));
        practitioner.setUserName(resultSet.getString("matricula"));
        practitioner.setName(resultSet.getString("name"));
        practitioner.setLastName(resultSet.getString("last_name"));
        practitioner.setEmail(resultSet.getString("email"));
        return practitioner;
    }

    private Practitioner mapResultSetToPractitioner(ResultSet resultSet) throws SQLException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(resultSet.getInt("user_id"));
        practitioner.setPassword(resultSet.getString("password"));
        practitioner.setName(resultSet.getString("name"));
        practitioner.setLastName(resultSet.getString("last_name"));
        practitioner.setEmail(resultSet.getString("email"));
        practitioner.setStatus(resolveNullableStatus(resultSet));
        practitioner.setGender(resolveNullableGender(resultSet));
        practitioner.setEnrollment(resultSet.getString("matricula"));
        practitioner.setUserName(resultSet.getString("matricula"));
        practitioner.setIndigenousLanguage(resultSet.getString("indigenous_language"));
        practitioner.setGrade(resultSet.getDouble("grade"));
        practitioner.setGroupId(resolveNullableGroupId(resultSet));
        return practitioner;
    }

    private UserStatus resolveNullableStatus(ResultSet resultSet) throws SQLException {
        String statusValue = resultSet.getString("status");
        return statusValue != null ? UserStatus.fromString(statusValue) : null;
    }

    private Gender resolveNullableGender(ResultSet resultSet) throws SQLException {
        String genderValue = resultSet.getString("gender");
        return genderValue != null ? Gender.fromDatabaseValue(genderValue) : null;
    }

    private Integer resolveNullableGroupId(ResultSet resultSet) throws SQLException {
        int groupId = resultSet.getInt("group_id");
        return resultSet.wasNull() ? null : groupId;
    }
}