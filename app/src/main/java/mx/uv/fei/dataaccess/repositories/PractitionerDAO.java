package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class PractitionerDAO extends BaseDAO implements IPractitionerDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO practitioner (practitioner_id, indigenous_language, grade) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT u.user_id, u.username AS matricula, u.password, u.name, u.last_name, u.email, u.status, u.gender, p.indigenous_language, p.grade FROM practitioner p INNER JOIN user u ON p.practitioner_id = u.user_id WHERE p.practitioner_id = ?";
    private static final String SQL_SELECT_ALL = "SELECT u.user_id, u.username AS matricula, u.password, u.name, u.last_name, u.email, u.status, u.gender, p.indigenous_language, p.grade FROM practitioner p INNER JOIN user u ON p.practitioner_id = u.user_id";
    private static final String SQL_UPDATE_PRACTITIONER = "UPDATE practitioner SET indigenous_language = ?, grade = ? WHERE practitioner_id = ?";

    @Inject
    public PractitionerDAO(IDatabaseConnection databaseConnection, UserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertPractitioner(Practitioner practitioner) throws DAOException {
        int resultId = -1;
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int generatedUserId = userDAO.insertUser(practitioner, connection);
                if (generatedUserId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT)) {
                        statement.setInt(1, generatedUserId);
                        statement.setString(2, practitioner.getIndigenousLanguage());
                        statement.setDouble(3, practitioner.getGrade());
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
                throw new DAOException("SQL Error al insertar el practicante. Cambios revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }
        return resultId;
    }

    @Override
    public Practitioner recoverPractitioner(int practitionerId) throws DAOException {
        Practitioner practitionerToSearch = new Practitioner();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {
            statement.setInt(1, practitionerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    practitionerToSearch.setId(resultSet.getInt("user_id"));
                    practitionerToSearch.setPassword(resultSet.getString("password"));
                    practitionerToSearch.setName(resultSet.getString("name"));
                    practitionerToSearch.setLastName(resultSet.getString("last_name"));
                    practitionerToSearch.setEmail(resultSet.getString("email"));

                    String statusValue = resultSet.getString("status");
                    practitionerToSearch.setStatus(statusValue != null ? UserStatus.fromString(statusValue) : null);

                    String genderValue = resultSet.getString("gender");
                    practitionerToSearch.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

                    practitionerToSearch.setEnrollment(resultSet.getString("matricula"));
                    practitionerToSearch.setIndigenousLanguage(resultSet.getString("indigenous_language"));
                    practitionerToSearch.setGrade(resultSet.getDouble("grade"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el practicante de la base de datos.", e);
        }
        return practitionerToSearch;
    }

    @Override
    public List<Practitioner> getAllPractitioners() throws DAOException {
        return recoverALL(SQL_SELECT_ALL, resultSet -> {
            Practitioner practitionerRecovered = new Practitioner();
            practitionerRecovered.setId(resultSet.getInt("user_id"));
            practitionerRecovered.setPassword(resultSet.getString("password"));
            practitionerRecovered.setName(resultSet.getString("name"));
            practitionerRecovered.setLastName(resultSet.getString("last_name"));
            practitionerRecovered.setEmail(resultSet.getString("email"));

            String statusValue = resultSet.getString("status");
            practitionerRecovered.setStatus(statusValue != null ? UserStatus.fromString(statusValue) : null);

            String genderValue = resultSet.getString("gender");
            practitionerRecovered.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

            practitionerRecovered.setEnrollment(resultSet.getString("matricula"));
            practitionerRecovered.setIndigenousLanguage(resultSet.getString("indigenous_language"));
            practitionerRecovered.setGrade(resultSet.getDouble("grade"));
            return practitionerRecovered;
        });
    }

    @Override
    public boolean updatePractitioner(Practitioner practitionerToUpdate, int id) throws DAOException {
        boolean isUpdated = false;
        practitionerToUpdate.setId(id);
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                boolean userUpdated = userDAO.updateUser(practitionerToUpdate, connection);
                if (userUpdated) {
                    try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PRACTITIONER)) {
                        statement.setString(1, practitionerToUpdate.getIndigenousLanguage());
                        statement.setDouble(2, practitionerToUpdate.getGrade());
                        statement.setInt(3, id);
                        if (statement.executeUpdate() > 0) {
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
                throw new DAOException("SQL Error al actualizar el practicante. Cambios revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }
        return isUpdated;
    }

    @Override
    public List<Practitioner> retrievePractitionersPendingAssignment() throws DAOException {
        List<Practitioner> pendingPractitionersList = new ArrayList<>();
        String queryToExecute = "SELECT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                "FROM practitioner p INNER JOIN user u ON p.practitioner_id = u.user_id " +
                "WHERE p.practitioner_id IN (SELECT practitioner_id FROM project_postulation) " +
                "AND p.practitioner_id NOT IN (SELECT practitioner_id FROM project_postulation WHERE postulation_status = 'Assigned')";

        try (Connection currentDatabaseConnection = databaseConnection.getConnection();
             PreparedStatement selectStatement = currentDatabaseConnection.prepareStatement(queryToExecute);
             ResultSet executionResultSet = selectStatement.executeQuery()) {
            while (executionResultSet.next()) {
                Practitioner currentPractitioner = new Practitioner();
                currentPractitioner.setId(executionResultSet.getInt("user_id"));
                currentPractitioner.setEnrollment(executionResultSet.getString("matricula"));
                currentPractitioner.setName(executionResultSet.getString("name"));
                currentPractitioner.setLastName(executionResultSet.getString("last_name"));
                currentPractitioner.setEmail(executionResultSet.getString("email"));
                pendingPractitionersList.add(currentPractitioner);
            }
        } catch (SQLException executionException) {
            throw new DAOException("Ocurrio un error al consultar los practicantes pendientes de asignacion.", executionException);
        }
        return pendingPractitionersList;
    }
}