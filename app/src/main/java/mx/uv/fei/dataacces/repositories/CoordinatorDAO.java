package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Coordinator;

@Component
public class CoordinatorDAO extends BaseDAO implements ICoordinatorDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO COORDINADOR (ID_COORDINADOR) VALUES (?)";
    private static final String SQL_SELECT_ONE = "SELECT U.ID_USUARIO, U.NOMBRE_USUARIO, U.CORREO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA FROM COORDINADOR C INNER JOIN USUARIO U ON C.ID_COORDINADOR = U.ID_USUARIO WHERE C.ID_COORDINADOR = ?";
    private static final String SQL_SELECT_ALL = "SELECT U.ID_USUARIO, U.NOMBRE_USUARIO, U.CORREO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA FROM COORDINADOR C INNER JOIN USUARIO U ON C.ID_COORDINADOR = U.ID_USUARIO";
    private static final String SQL_SELECT_CURRENT = "SELECT U.ID_USUARIO, U.NOMBRE_USUARIO, U.CORREO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.ESTADO, U.GENERO, U.FECHA_REGISTRO, U.FECHA_BAJA FROM COORDINADOR C INNER JOIN USUARIO U ON C.ID_COORDINADOR = U.ID_USUARIO WHERE U.ESTADO IN ('Activo', 'Pendiente')";

    @Inject
    public CoordinatorDAO(IDatabaseConnection databaseConnection, UserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertCoordinator(Coordinator coordinator) throws DAOException {
        int resultId = -1;

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int generatedUserId = userDAO.insertUser(coordinator, connection);

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

            } catch (SQLException exception) {
                connection.rollback();
                throw new DAOException("SQL Error al intentar insertar el coordinador. Cambios revertidos.", exception);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw new DAOException("Error crítico de conexión a la base de datos.", exception);
        }

        return resultId;
    }

    @Override
    public Coordinator recoverCoordinator(int coordinatorId) throws DAOException {
        Coordinator coordinatorToSearch = new Coordinator();

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {

            statement.setInt(1, coordinatorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    coordinatorToSearch.setId(resultSet.getInt("ID_USUARIO"));
                    coordinatorToSearch.setUserName(resultSet.getString("NOMBRE_USUARIO"));
                    coordinatorToSearch.setEmail(resultSet.getString("CORREO"));
                    coordinatorToSearch.setPassword(resultSet.getString("PASSWORD"));
                    coordinatorToSearch.setName(resultSet.getString("NOMBRE"));
                    coordinatorToSearch.setLastName(resultSet.getString("APELLIDOS"));
                    coordinatorToSearch.setStatus(resultSet.getString("ESTADO"));
                    coordinatorToSearch.setGender(resultSet.getString("GENERO"));

                    if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
                        coordinatorToSearch.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
                    }

                    if (resultSet.getTimestamp("FECHA_BAJA") != null) {
                        coordinatorToSearch.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
                    }
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al intentar recuperar el coordinador de la base de datos.", exception);
        }
        return coordinatorToSearch;
    }

    @Override
    public Coordinator getCurrentCoordinator() throws DAOException {
        Coordinator currentCoordinator = null;

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_CURRENT);
                ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                currentCoordinator = new Coordinator();
                currentCoordinator.setId(resultSet.getInt("ID_USUARIO"));
                currentCoordinator.setUserName(resultSet.getString("NOMBRE_USUARIO"));
                currentCoordinator.setEmail(resultSet.getString("CORREO"));
                currentCoordinator.setPassword(resultSet.getString("PASSWORD"));
                currentCoordinator.setName(resultSet.getString("NOMBRE"));
                currentCoordinator.setLastName(resultSet.getString("APELLIDOS"));
                currentCoordinator.setStatus(resultSet.getString("ESTADO"));
                currentCoordinator.setGender(resultSet.getString("GENERO"));

                if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
                    currentCoordinator.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
                }

                if (resultSet.getTimestamp("FECHA_BAJA") != null) {
                    currentCoordinator.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
                }
            }
        } catch (SQLException exception) {
            throw new DAOException("Error al consultar el coordinador actual en la base de datos.", exception);
        }
        return currentCoordinator;
    }

    @Override
    public List<Coordinator> getAllCoordinators() throws DAOException {
        return recoverALL(SQL_SELECT_ALL, resultSet -> {
            Coordinator coordinatorRecovered = new Coordinator();

            coordinatorRecovered.setId(resultSet.getInt("ID_USUARIO"));
            coordinatorRecovered.setUserName(resultSet.getString("NOMBRE_USUARIO"));
            coordinatorRecovered.setEmail(resultSet.getString("CORREO"));
            coordinatorRecovered.setPassword(resultSet.getString("PASSWORD"));
            coordinatorRecovered.setName(resultSet.getString("NOMBRE"));
            coordinatorRecovered.setLastName(resultSet.getString("APELLIDOS"));
            coordinatorRecovered.setStatus(resultSet.getString("ESTADO"));
            coordinatorRecovered.setGender(resultSet.getString("GENERO"));

            if (resultSet.getTimestamp("FECHA_REGISTRO") != null) {
                coordinatorRecovered.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
            }

            if (resultSet.getTimestamp("FECHA_BAJA") != null) {
                coordinatorRecovered.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
            }

            return coordinatorRecovered;
        });
    }

    @Override
    public boolean updateCoordinator(Coordinator coordinatorToUpdate, int id) throws DAOException {
        boolean isUpdated = false;
        coordinatorToUpdate.setId(id);

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                isUpdated = userDAO.updateUser(coordinatorToUpdate, connection);

                if (isUpdated) {
                    connection.commit();
                } else {
                    connection.rollback();
                }

            } catch (SQLException exception) {
                connection.rollback();
                throw new DAOException("SQL Error al intentar actualizar el coordinador. Cambios revertidos.", exception);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException exception) {
            throw new DAOException("Error crítico de conexión a la base de datos.", exception);
        }

        return isUpdated;
    }
}