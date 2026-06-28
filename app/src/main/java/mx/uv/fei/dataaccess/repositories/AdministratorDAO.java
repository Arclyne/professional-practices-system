package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IAdministratorDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Acceso a datos de los administradores del sistema.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class AdministratorDAO extends BaseDAO implements IAdministratorDAO {

    private static final String SQL_INSERT_ADMINISTRATOR =
            "INSERT INTO administrator (administrator_id) VALUES (?)";
    private static final String SQL_SELECT_ADMINISTRATOR_BY_ID =
            "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
                    "FROM administrator a " +
                    "INNER JOIN user u ON a.administrator_id = u.user_id " +
                    "WHERE a.administrator_id = ?";
    private static final String SQL_SELECT_ALL_ADMINISTRATORS =
            "SELECT u.user_id, u.username, u.password, u.name, u.last_name, u.email, u.role_name, u.status, u.gender, u.registration_date, u.discharge_date " +
                    "FROM administrator a " +
                    "INNER JOIN user u ON a.administrator_id = u.user_id";
    private static final String SQL_CHECK_ADMINISTRATOR_EXISTS =
            "SELECT COUNT(*) FROM administrator";

    private final IUserDAO userDAO;

    /**
     * Crea el DAO de administradores con la fuente de conexiones y el DAO de usuarios.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     * @param userDAO            DAO de usuarios usado para los datos comunes de cuenta
     */
    @Inject
    public AdministratorDAO(IDatabaseConnection databaseConnection, IUserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    /**
     * Indica si ya existe al menos un administrador registrado en el sistema.
     *
     * @return {@code true} si existe algún administrador; {@code false} en caso contrario
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    public boolean checkIfAdminExists() throws DAOException {
        boolean adminExists = false;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_CHECK_ADMINISTRATOR_EXISTS);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                adminExists = resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new DAOException("Error al verificar la existencia del administrador en la base de datos.", e);
        }

        return adminExists;
    }

    /**
     * Inserta un administrador creando primero su usuario base dentro de una transacción.
     *
     * @param administrator administrador con los datos a registrar
     * @return identificador generado para el administrador, o {@code -1} si la operación falla
     * @throws DAOException si ocurre un error y se revierte la transacción
     */
    @Override
    public int insertAdministrator(Administrator administrator) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int generatedUserId = userDAO.insertUser(administrator, connection);

                if (generatedUserId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_ADMINISTRATOR)) {
                        statement.setInt(1, generatedUserId);
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
                throw new DAOException("Error al insertar el administrador. Se ha hecho un rollback.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }

        return generatedId;
    }

    /**
     * Recupera un administrador junto con sus datos de usuario a partir de su identificador.
     *
     * @param administratorId identificador del administrador a recuperar
     * @return administrador encontrado, o un {@link Administrator} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public Administrator recoverAdministrator(int administratorId) throws DAOException {
        Administrator recoveredAdministrator = new Administrator();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ADMINISTRATOR_BY_ID)) {

            statement.setInt(1, administratorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredAdministrator = mapResultSetToAdministrator(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el administrador de la base de datos.", e);
        }

        return recoveredAdministrator;
    }

    /**
     * Recupera todos los administradores junto con sus datos de usuario.
     *
     * @return lista con todos los administradores; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Administrator> getAllAdministrators() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_ADMINISTRATORS, this::mapResultSetToAdministrator);
    }

    /**
     * Actualiza los datos de usuario asociados a un administrador.
     *
     * @param administratorToUpdate administrador con los datos modificados
     * @param administratorId       identificador del administrador a actualizar
     * @throws DAOException si ocurre un error al actualizar
     */
    @Override
    public void updateAdministrator(Administrator administratorToUpdate, int administratorId) throws DAOException {
        administratorToUpdate.setId(administratorId);

        try (Connection connection = databaseConnection.getConnection()) {
            userDAO.updateUser(administratorToUpdate, connection);
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión al actualizar administrador.", e);
        }
    }

    /**
     * Construye un administrador con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return administrador con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Administrator mapResultSetToAdministrator(ResultSet resultSet) throws SQLException {
        Administrator administrator = new Administrator();

        administrator.setId(resultSet.getInt("user_id"));
        administrator.setUserName(resultSet.getString("username"));
        administrator.setPassword(resultSet.getString("password"));
        administrator.setName(resultSet.getString("name"));
        administrator.setLastName(resultSet.getString("last_name"));
        administrator.setEmail(resultSet.getString("email"));
        administrator.setRole(resultSet.getString("role_name"));
        administrator.setStatus(resolveNullableStatus(resultSet));
        administrator.setGender(resolveNullableGender(resultSet));
        administrator.setRegistrationDate(resolveNullableTimestamp(resultSet, "registration_date"));
        administrator.setDischargeDate(resolveNullableTimestamp(resultSet, "discharge_date"));

        return administrator;
    }

    /**
     * Obtiene el estado del usuario tolerando valores nulos en la columna.
     *
     * @param resultSet resultado posicionado en la fila a leer
     * @return estado del usuario, o {@code null} si la columna es nula
     * @throws SQLException si ocurre un error al leer la columna
     */
    private UserStatus resolveNullableStatus(ResultSet resultSet) throws SQLException {
        String statusValue = resultSet.getString("status");
        return statusValue != null ? UserStatus.fromString(statusValue) : null;
    }

    /**
     * Obtiene el género del usuario tolerando valores nulos en la columna.
     *
     * @param resultSet resultado posicionado en la fila a leer
     * @return género del usuario, o {@code null} si la columna es nula
     * @throws SQLException si ocurre un error al leer la columna
     */
    private Gender resolveNullableGender(ResultSet resultSet) throws SQLException {
        String genderValue = resultSet.getString("gender");
        return genderValue != null ? Gender.fromDatabaseValue(genderValue) : null;
    }

    /**
     * Obtiene una marca de tiempo como {@link LocalDateTime} tolerando valores nulos.
     *
     * @param resultSet  resultado posicionado en la fila a leer
     * @param columnName nombre de la columna de tipo marca de tiempo
     * @return fecha y hora correspondiente, o {@code null} si la columna es nula
     * @throws SQLException si ocurre un error al leer la columna
     */
    private LocalDateTime resolveNullableTimestamp(ResultSet resultSet, String columnName) throws SQLException {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}