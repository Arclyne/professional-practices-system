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
import mx.uv.fei.dataacces.interfaces.IAdministratorDAO;
import mx.uv.fei.domain.dto.Administrator;

@Component
public class AdministratorDAO extends BaseDAO implements IAdministratorDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO ADMINISTRADOR (ID_ADMINISTRADOR) VALUES (?)";
    private static final String SQL_SELECT_ONE = "SELECT U.ID_USUARIO, U.USER, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.NOMBRE_ROL, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA FROM ADMINISTRADOR A INNER JOIN USUARIO U ON A.ID_ADMINISTRADOR = U.ID_USUARIO WHERE A.ID_ADMINISTRADOR = ?";
    private static final String SQL_SELECT_ALL = "SELECT U.ID_USUARIO, U.USER, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.NOMBRE_ROL, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA FROM ADMINISTRADOR A INNER JOIN USUARIO U ON A.ID_ADMINISTRADOR = U.ID_USUARIO";
    private static final String SQL_CHECK_EXISTS = "SELECT COUNT(*) FROM ADMINISTRADOR";

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

        try (
                Connection connection = databaseConnection.getConnection();
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
        admin.setId(resultSet.getInt("ID_USUARIO"));
        admin.setUserName(resultSet.getString("USER"));
        admin.setPassword(resultSet.getString("PASSWORD"));
        admin.setName(resultSet.getString("NOMBRE"));
        admin.setLastName(resultSet.getString("APELLIDOS"));
        admin.setEmail(resultSet.getString("CORREO"));
        admin.setRole(resultSet.getString("NOMBRE_ROL"));
        admin.setStatus(resultSet.getString("ESTADO"));
        admin.setGender(resultSet.getString("GENERO"));

        if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
            admin.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
        }

        if (resultSet.getTimestamp("FECHA_BAJA") != null) {
            admin.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
        }
    }
}