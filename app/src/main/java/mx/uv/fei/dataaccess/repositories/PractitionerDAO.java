package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class PractitionerDAO extends BaseDAO implements IPractitionerDAO {

    private static final String SQL_INSERT_PRACTITIONER = "INSERT INTO practitioner (practitioner_id, indigenous_language, grade, group_id) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_PRACTITIONER_BY_ID = "SELECT u.user_id, u.username AS matricula, u.password, u.name, u.last_name, u.email, u.status, u.gender, p.indigenous_language, p.grade, p.group_id FROM practitioner p INNER JOIN user u ON p.practitioner_id = u.user_id WHERE p.practitioner_id = ?";
    private static final String SQL_SELECT_ALL_PRACTITIONERS = "SELECT u.user_id, u.username AS matricula, u.password, u.name, u.last_name, u.email, u.status, u.gender, p.indigenous_language, p.grade, p.group_id FROM practitioner p INNER JOIN user u ON p.practitioner_id = u.user_id";
    private static final String SQL_UPDATE_PRACTITIONER = "UPDATE practitioner SET indigenous_language = ?, grade = ?, group_id = ? WHERE practitioner_id = ?";

    private static final String SQL_SELECT_PENDING_ASSIGNMENT_PRACTITIONERS = "SELECT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
            "FROM practitioner p INNER JOIN user u ON p.practitioner_id = u.user_id " +
            "WHERE p.practitioner_id IN (SELECT practitioner_id FROM project_postulation) " +
            "AND p.practitioner_id NOT IN (SELECT practitioner_id FROM project_postulation WHERE postulation_status = 'Assigned')";

    private static final String MSG_INSERT_ROLLBACK = "SQL error while inserting the practitioner. Changes reverted.";
    private static final String MSG_CRITICAL_CONN = "Critical database connection error.";
    private static final String MSG_RECOVER_ERROR = "Database error while attempting to recover the practitioner.";
    private static final String MSG_UPDATE_ROLLBACK = "SQL error while updating the practitioner. Changes reverted.";
    private static final String MSG_PENDING_ERROR = "An error occurred while querying practitioners pending assignment.";

    private final IUserDAO userDAO;

    @Inject
    public PractitionerDAO(IDatabaseConnection databaseConnection, IUserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertPractitioner(Practitioner practitioner) throws DAOException {
        int resultId = -1;

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                resultId = executePractitionerInsertTransaction(connection, practitioner);

                if (resultId > 0) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException | DAOException e) {
                connection.rollback();
                throw new DAOException(MSG_INSERT_ROLLBACK, e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException(MSG_CRITICAL_CONN, e);
        }

        return resultId;
    }

    private int executePractitionerInsertTransaction(Connection connection, Practitioner practitioner) throws SQLException, DAOException {
        int insertedPractitionerId = -1;
        int generatedUserId = userDAO.insertUser(practitioner, connection);

        if (generatedUserId > 0) {
            try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PRACTITIONER)) {
                statement.setInt(1, generatedUserId);
                statement.setString(2, practitioner.getIndigenousLanguage());
                statement.setDouble(3, practitioner.getGrade());

                if (practitioner.getGroupId() != null) {
                    statement.setInt(4, practitioner.getGroupId());
                } else {
                    statement.setNull(4, Types.INTEGER);
                }

                if (statement.executeUpdate() > 0) {
                    insertedPractitionerId = generatedUserId;
                }
            }
        }

        return insertedPractitionerId;
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
            throw new DAOException(MSG_RECOVER_ERROR, e);
        }

        return recoveredPractitioner;
    }

    @Override
    public List<Practitioner> getAllPractitioners() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_PRACTITIONERS, this::mapResultSetToPractitioner);
    }

    @Override
    public boolean updatePractitioner(Practitioner practitionerToUpdate, int practitionerId) throws DAOException {
        boolean isUpdated = false;
        practitionerToUpdate.setId(practitionerId);

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                isUpdated = executePractitionerUpdateTransaction(connection, practitionerToUpdate, practitionerId);

                if (isUpdated) {
                    connection.commit();
                } else {
                    connection.rollback();
                }
            } catch (SQLException | DAOException e) {
                connection.rollback();
                throw new DAOException(MSG_UPDATE_ROLLBACK, e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException(MSG_CRITICAL_CONN, e);
        }

        return isUpdated;
    }

    private boolean executePractitionerUpdateTransaction(Connection connection, Practitioner practitioner, int practitionerId) throws SQLException, DAOException {
        boolean updateSuccessful = false;
        boolean userUpdated = userDAO.updateUser(practitioner, connection);

        if (userUpdated) {
            try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PRACTITIONER)) {
                statement.setString(1, practitioner.getIndigenousLanguage());
                statement.setDouble(2, practitioner.getGrade());

                if (practitioner.getGroupId() != null) {
                    statement.setInt(3, practitioner.getGroupId());
                } else {
                    statement.setNull(3, Types.INTEGER);
                }

                statement.setInt(4, practitionerId);

                updateSuccessful = statement.executeUpdate() > 0;
            }
        }

        return updateSuccessful;
    }

    @Override
    public List<Practitioner> retrievePractitionersPendingAssignment() throws DAOException {
        List<Practitioner> pendingPractitionersList = new ArrayList<>();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement selectStatement = connection.prepareStatement(SQL_SELECT_PENDING_ASSIGNMENT_PRACTITIONERS);
             ResultSet resultSet = selectStatement.executeQuery()) {

            while (resultSet.next()) {
                Practitioner currentPractitioner = new Practitioner();
                currentPractitioner.setId(resultSet.getInt("user_id"));
                currentPractitioner.setEnrollment(resultSet.getString("matricula"));
                currentPractitioner.setName(resultSet.getString("name"));
                currentPractitioner.setLastName(resultSet.getString("last_name"));
                currentPractitioner.setEmail(resultSet.getString("email"));
                pendingPractitionersList.add(currentPractitioner);
            }
        } catch (SQLException e) {
            throw new DAOException(MSG_PENDING_ERROR, e);
        }

        return pendingPractitionersList;
    }

    private Practitioner mapResultSetToPractitioner(ResultSet resultSet) throws SQLException {
        Practitioner practitioner = new Practitioner();

        practitioner.setId(resultSet.getInt("user_id"));
        practitioner.setPassword(resultSet.getString("password"));
        practitioner.setName(resultSet.getString("name"));
        practitioner.setLastName(resultSet.getString("last_name"));
        practitioner.setEmail(resultSet.getString("email"));

        String statusValue = resultSet.getString("status");
        practitioner.setStatus(statusValue != null ? UserStatus.fromString(statusValue) : null);

        String genderValue = resultSet.getString("gender");
        practitioner.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

        practitioner.setEnrollment(resultSet.getString("matricula"));
        practitioner.setIndigenousLanguage(resultSet.getString("indigenous_language"));
        practitioner.setGrade(resultSet.getDouble("grade"));

        int retrievedGroupId = resultSet.getInt("group_id");
        if (resultSet.wasNull()) {
            practitioner.setGroupId(null);
        } else {
            practitioner.setGroupId(retrievedGroupId);
        }

        return practitioner;
    }
}