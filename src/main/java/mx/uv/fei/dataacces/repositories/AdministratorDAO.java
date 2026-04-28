package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IAdministratorDAO;
import mx.uv.fei.domain.dto.Administrator;

public class AdministratorDAO extends BaseDAO implements IAdministratorDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO ADMINISTRADOR (ID_ADMINISTRADOR, NUMERO_PERSONAL) VALUES (?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT U.ID_USUARIO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA, A.NUMERO_PERSONAL FROM ADMINISTRADOR A INNER JOIN USUARIO U ON A.ID_ADMINISTRADOR = U.ID_USUARIO WHERE A.ID_ADMINISTRADOR = ?";
    private static final String SQL_SELECT_ALL = "SELECT U.ID_USUARIO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA, A.NUMERO_PERSONAL FROM ADMINISTRADOR A INNER JOIN USUARIO U ON A.ID_ADMINISTRADOR = U.ID_USUARIO";
    private static final String SQL_UPDATE_ADMIN = "UPDATE ADMINISTRADOR SET NUMERO_PERSONAL = ? WHERE ID_ADMINISTRADOR = ?";
    private static final String SQL_CHECK_EXISTS = "SELECT COUNT(*) FROM ADMINISTRADOR";

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
                        statement.setString(2, administrator.getStaffNumber());

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
                    adminToSearch.setId(resultSet.getInt("ID_USUARIO"));
                    adminToSearch.setPassword(resultSet.getString("PASSWORD"));
                    adminToSearch.setName(resultSet.getString("NOMBRE"));
                    adminToSearch.setLastName(resultSet.getString("APELLIDOS"));
                    adminToSearch.setEmail(resultSet.getString("CORREO"));
                    adminToSearch.setStatus(resultSet.getString("ESTADO"));
                    adminToSearch.setGender(resultSet.getString("GENERO"));

                    adminToSearch.setStaffNumber(resultSet.getString("NUMERO_PERSONAL"));

                    if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
                        adminToSearch.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
                    }

                    if (resultSet.getTimestamp("FECHA_BAJA") != null) {
                        adminToSearch.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
                    }
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

            adminRecovered.setId(resultSet.getInt("ID_USUARIO"));
            adminRecovered.setPassword(resultSet.getString("PASSWORD"));
            adminRecovered.setName(resultSet.getString("NOMBRE"));
            adminRecovered.setLastName(resultSet.getString("APELLIDOS"));
            adminRecovered.setEmail(resultSet.getString("CORREO"));
            adminRecovered.setStatus(resultSet.getString("ESTADO"));
            adminRecovered.setGender(resultSet.getString("GENERO"));

            adminRecovered.setStaffNumber(resultSet.getString("NUMERO_PERSONAL"));

            if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
                adminRecovered.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
            }
            if (resultSet.getTimestamp("FECHA_BAJA") != null) {
                adminRecovered.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
            }

            return adminRecovered;
        });
    }

    @Override
    public boolean updateAdministrator(Administrator adminToUpdate, int id) throws DAOException {
        boolean isUpdated = false;
        adminToUpdate.setId(id);

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                boolean userUpdated = userDAO.updateUser(adminToUpdate, connection);

                if (userUpdated) {
                    try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_ADMIN)) {
                        statement.setString(1, adminToUpdate.getStaffNumber());
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
                throw new DAOException("SQL Error al actualizar administrador. Se ha hecho un rollback.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }

        return isUpdated;
    }
}