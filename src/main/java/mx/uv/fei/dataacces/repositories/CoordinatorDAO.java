package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Coordinator;

@Repository
public class CoordinatorDAO extends BaseDAO implements ICoordinatorDAO {

    private final UserDAO userDAO;
    
    private static final String SQL_INSERT = "INSERT INTO COORDINADOR (ID_COORDINADOR) VALUES (?)";
    private static final String SQL_SELECT_ONE = "SELECT U.ID_USUARIO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.ESTADO, U.GENERO, C.FECHA_REGISTRO, C.FECHA_BAJA FROM COORDINADOR C INNER JOIN USUARIO U ON C.ID_COORDINADOR = U.ID_USUARIO WHERE C.ID_COORDINADOR = ?";
    private static final String SQL_SELECT_ALL = "SELECT U.ID_USUARIO, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.ESTADO, U.GENERO, C.FECHA_REGISTRO, C.FECHA_BAJA FROM COORDINADOR C INNER JOIN USUARIO U ON C.ID_COORDINADOR = U.ID_USUARIO";

    @Autowired
    public CoordinatorDAO(IDatabaseConnection dbConnection, UserDAO userDAO) {
        super(dbConnection);
        this.userDAO = userDAO;
    }

    @Override
    public int insertCoordinator(Coordinator coordinator) throws DAOException {
        int resultId = -1;

        try (Connection connection = dbConnection.getConnection()) {
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

            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("SQL Error while inserting coordinator. Changes were rolled back.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Critical database connection error.", e);
        }

        return resultId;
    }

    @Override
    public Coordinator recoverCoordinator(int coordinatorId) throws DAOException {
        Coordinator coordinatorToSearch = new Coordinator();
        
        try (
                Connection connection = dbConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {
            
            statement.setInt(1, coordinatorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    coordinatorToSearch.setId(resultSet.getInt("ID_USUARIO"));
                    coordinatorToSearch.setPassword(resultSet.getString("PASSWORD"));
                    coordinatorToSearch.setName(resultSet.getString("NOMBRE"));
                    coordinatorToSearch.setLastName(resultSet.getString("APELLIDOS"));
                    coordinatorToSearch.setStatus(resultSet.getString("ESTADO"));
                    coordinatorToSearch.setGender(resultSet.getString("GENERO"));
                    
                    coordinatorToSearch.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
                    
                    if (resultSet.getTimestamp("FECHA_BAJA") != null) {
                        coordinatorToSearch.setDischargeDate(resultSet.getTimestamp("FECHA_BAJA").toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar recuperar el coordinador de la base de datos.", e);
        }
        return coordinatorToSearch;
    }

    @Override
    public List<Coordinator> getAllCoordinators() throws DAOException {
        return recoverALL(SQL_SELECT_ALL, resultSet -> {
            Coordinator coordinatorRecovered = new Coordinator();
            
            coordinatorRecovered.setId(resultSet.getInt("ID_USUARIO"));
            coordinatorRecovered.setPassword(resultSet.getString("PASSWORD"));
            coordinatorRecovered.setName(resultSet.getString("NOMBRE"));
            coordinatorRecovered.setLastName(resultSet.getString("APELLIDOS"));
            coordinatorRecovered.setStatus(resultSet.getString("ESTADO"));
            coordinatorRecovered.setGender(resultSet.getString("GENERO"));
            
            coordinatorRecovered.setRegistrationDate(resultSet.getTimestamp("FECHA_REGISTRO").toLocalDateTime());
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

        try (Connection connection = dbConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                isUpdated = userDAO.updateUser(coordinatorToUpdate, connection);

                if (isUpdated) {
                    connection.commit();
                } else {
                    connection.rollback();
                }

            } catch (SQLException e) {
                connection.rollback();
                throw new DAOException("SQL Error while updating coordinator. Changes were rolled back.", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new DAOException("Critical database connection error.", e);
        }

        return isUpdated;
    }
}