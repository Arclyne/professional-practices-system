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
import mx.uv.fei.dataacces.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.dto.Practitioner;

@Component
public class PractitionerDAO extends BaseDAO implements IPractitionerDAO {

    private final UserDAO userDAO;

    private static final String SQL_INSERT = "INSERT INTO PRACTICANTE (ID_PRACTICANTE, LENGUA_INDIGENA, CALIFICACION) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT U.ID_USUARIO, U.NOMBRE_USUARIO AS MATRICULA, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.ESTADO, U.GENERO, P.LENGUA_INDIGENA, P.CALIFICACION FROM PRACTICANTE P INNER JOIN USUARIO U ON P.ID_PRACTICANTE = U.ID_USUARIO WHERE P.ID_PRACTICANTE = ?";
    private static final String SQL_SELECT_ALL = "SELECT U.ID_USUARIO, U.NOMBRE_USUARIO AS MATRICULA, U.PASSWORD, U.NOMBRE, U.APELLIDOS, U.CORREO, U.ESTADO, U.GENERO, P.LENGUA_INDIGENA, P.CALIFICACION FROM PRACTICANTE P INNER JOIN USUARIO U ON P.ID_PRACTICANTE = U.ID_USUARIO";

    private static final String SQL_UPDATE_PRACTITIONER = "UPDATE PRACTICANTE SET LENGUA_INDIGENA = ?, CALIFICACION = ? WHERE ID_PRACTICANTE = ?";

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

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE);) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    practitionerToSearch.setId(resultSet.getInt("ID_USUARIO"));
                    practitionerToSearch.setPassword(resultSet.getString("PASSWORD"));
                    practitionerToSearch.setName(resultSet.getString("NOMBRE"));
                    practitionerToSearch.setLastName(resultSet.getString("APELLIDOS"));
                    practitionerToSearch.setEmail(resultSet.getString("CORREO"));
                    practitionerToSearch.setStatus(resultSet.getString("ESTADO"));
                    practitionerToSearch.setGender(resultSet.getString("GENERO"));
                    practitionerToSearch.setEnrollment(resultSet.getString("MATRICULA"));
                    practitionerToSearch.setIndigenousLanguage(resultSet.getString("LENGUA_INDIGENA"));
                    practitionerToSearch.setGrade(resultSet.getDouble("CALIFICACION"));
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

            practitionerRecovered.setId(resultSet.getInt("ID_USUARIO"));
            practitionerRecovered.setPassword(resultSet.getString("PASSWORD"));
            practitionerRecovered.setName(resultSet.getString("NOMBRE"));
            practitionerRecovered.setLastName(resultSet.getString("APELLIDOS"));
            practitionerRecovered.setEmail(resultSet.getString("CORREO"));
            practitionerRecovered.setStatus(resultSet.getString("ESTADO"));
            practitionerRecovered.setGender(resultSet.getString("GENERO"));

            practitionerRecovered.setEnrollment(resultSet.getString("MATRICULA"));
            practitionerRecovered.setIndigenousLanguage(resultSet.getString("LENGUA_INDIGENA"));
            practitionerRecovered.setGrade(resultSet.getDouble("CALIFICACION"));

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
}