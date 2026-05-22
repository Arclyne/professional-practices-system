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
import mx.uv.fei.dataaccess.interfaces.IAdministratorDAO;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

@Component
public class AdministratorDAO extends BaseDAO implements IAdministratorDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO administrator (administrator_id) VALUES (?)";

    private static final String SQL_SELECT_ONE = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
            "FROM administrator a " +
            "INNER JOIN user u ON a.administrator_id = u.user_id " +
            "WHERE a.administrator_id = ?";

    private static final String SQL_SELECT_ALL = "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
            "FROM administrator a " +
            "INNER JOIN user u ON a.administrator_id = u.user_id";

    private static final String SQL_CHECK_EXISTS = "SELECT COUNT(*) FROM administrator";

    @Inject
    public AdministratorDAO(IDatabaseConnection databaseConnection, UserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    public boolean checkIfAdminExists() throws DAOException {
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_CHECK_EXISTS);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al verificar la existencia del administrador en la base de datos.", e);
        }
        return false;
    }

    @Override
    public int insertAdministrator(Administrator administrator) throws DAOException {
        int resultId = -1;
        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int generatedUserId = userDAO.insertUser(administrator, connection);
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
                throw new DAOException("SQL Error al insertar el administrador. Se ha hecho un rollback.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }
        return resultId;
    }

    @Override
    public Administrator recoverAdministrator(int administratorId) throws DAOException {
        Administrator adminToSearch = new Administrator();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {
            statement.setInt(1, administratorId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    mapAdministrator(adminToSearch, resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el administrador de la base de datos.", e);
        }
        return adminToSearch;
    }

    @Override
    public List<Administrator> getAllAdministrators() throws DAOException {
        return recoverALL(SQL_SELECT_ALL, resultSet -> {
            Administrator adminRecovered = new Administrator();
            mapAdministrator(adminRecovered, resultSet);
            return adminRecovered;
        });
    }

    @Override
    public boolean updateAdministrator(Administrator adminToUpdate, int id) throws DAOException {
        adminToUpdate.setId(id);
        try (Connection connection = databaseConnection.getConnection()) {
            return userDAO.updateUser(adminToUpdate, connection);
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión al actualizar administrador.", e);
        }
    }

    private void mapAdministrator(Administrator admin, ResultSet resultSet) throws SQLException {
        admin.setId(resultSet.getInt("user_id"));
        admin.setUserName(resultSet.getString("username"));
        admin.setPassword(resultSet.getString("password"));
        admin.setName(resultSet.getString("name"));
        admin.setLastName(resultSet.getString("last_name"));
        admin.setEmail(resultSet.getString("email"));
        admin.setRole(resultSet.getString("role_name"));

        String statusValue = resultSet.getString("status");
        admin.setStatus(statusValue != null ? UserStatus.fromString(statusValue) : null);

        String genderValue = resultSet.getString("gender");
        admin.setGender(genderValue != null ? Gender.fromDatabaseValue(genderValue) : null);

        if (resultSet.getTimestamp("registration_date") != null) {
            admin.setRegistrationDate(resultSet.getTimestamp("registration_date").toLocalDateTime());
        }
        if (resultSet.getTimestamp("discharge_date") != null) {
            admin.setDischargeDate(resultSet.getTimestamp("discharge_date").toLocalDateTime());
        }
    }
}