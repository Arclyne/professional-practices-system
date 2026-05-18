package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class PractitionerDAO extends BaseDAO implements IPractitionerDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO practitioner (ID_PRACTITIONER, INDIGENOUS_LANGUAGE, GRADE) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT u.ID_USER, u.USERNAME AS matricula, u.PASSWORD, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.STATUS, u.GENDER, p.INDIGENOUS_LANGUAGE, p.GRADE FROM practitioner p INNER JOIN user u ON p.ID_PRACTITIONER = u.ID_USER WHERE p.ID_PRACTITIONER = ?";
    private static final String SQL_SELECT_ALL = "SELECT u.ID_USER, u.USERNAME AS matricula, u.PASSWORD, u.FIRST_NAME, u.LAST_NAME, u.EMAIL, u.STATUS, u.GENDER, p.INDIGENOUS_LANGUAGE, p.GRADE FROM practitioner p INNER JOIN user u ON p.ID_PRACTITIONER = u.ID_USER";
    private static final String SQL_UPDATE_PRACTITIONER = "UPDATE practitioner SET INDIGENOUS_LANGUAGE = ?, GRADE = ? WHERE ID_PRACTITIONER = ?";

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
                    practitionerToSearch.setId(resultSet.getInt("ID_USER"));
                    practitionerToSearch.setPassword(resultSet.getString("PASSWORD"));
                    practitionerToSearch.setName(resultSet.getString("FIRST_NAME"));
                    practitionerToSearch.setLastName(resultSet.getString("LAST_NAME"));
                    practitionerToSearch.setEmail(resultSet.getString("EMAIL"));
                    practitionerToSearch.setStatus(UserStatus.fromString(resultSet.getString("STATUS")));

                    String genderValue = resultSet.getString("GENDER");
                    practitionerToSearch.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

                    practitionerToSearch.setEnrollment(resultSet.getString("matricula"));
                    practitionerToSearch.setIndigenousLanguage(resultSet.getString("INDIGENOUS_LANGUAGE"));
                    practitionerToSearch.setGrade(resultSet.getDouble("GRADE"));
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
            practitionerRecovered.setId(resultSet.getInt("ID_USER"));
            practitionerRecovered.setPassword(resultSet.getString("PASSWORD"));
            practitionerRecovered.setName(resultSet.getString("FIRST_NAME"));
            practitionerRecovered.setLastName(resultSet.getString("LAST_NAME"));
            practitionerRecovered.setEmail(resultSet.getString("EMAIL"));
            practitionerRecovered.setStatus(UserStatus.fromString(resultSet.getString("STATUS")));

            String genderValue = resultSet.getString("GENDER");
            practitionerRecovered.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

            practitionerRecovered.setEnrollment(resultSet.getString("matricula"));
            practitionerRecovered.setIndigenousLanguage(resultSet.getString("INDIGENOUS_LANGUAGE"));
            practitionerRecovered.setGrade(resultSet.getDouble("GRADE"));
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
        String queryToExecute = "SELECT u.ID_USER, u.USERNAME AS matricula, u.FIRST_NAME, u.LAST_NAME, u.EMAIL " +
                "FROM practitioner p INNER JOIN user u ON p.ID_PRACTITIONER = u.ID_USER " +
                "WHERE p.ID_PRACTITIONER IN (SELECT ID_PRACTITIONER FROM project_application) " +
                "AND p.ID_PRACTITIONER NOT IN (SELECT ID_PRACTITIONER FROM project_application WHERE APPLICATION_STATUS = 'Assigned')";

        try (Connection currentDatabaseConnection = databaseConnection.getConnection();
             PreparedStatement selectStatement = currentDatabaseConnection.prepareStatement(queryToExecute);
             ResultSet executionResultSet = selectStatement.executeQuery()) {
            while (executionResultSet.next()) {
                Practitioner currentPractitioner = new Practitioner();
                currentPractitioner.setId(executionResultSet.getInt("ID_USER"));
                currentPractitioner.setEnrollment(executionResultSet.getString("matricula"));
                currentPractitioner.setName(executionResultSet.getString("FIRST_NAME"));
                currentPractitioner.setLastName(executionResultSet.getString("LAST_NAME"));
                currentPractitioner.setEmail(executionResultSet.getString("EMAIL"));
                pendingPractitionersList.add(currentPractitioner);
            }
        } catch (SQLException executionException) {
            throw new DAOException("Ocurrio un error al consultar los practicantes pendientes de asignacion.", executionException);
        }
        return pendingPractitionersList;
    }
}