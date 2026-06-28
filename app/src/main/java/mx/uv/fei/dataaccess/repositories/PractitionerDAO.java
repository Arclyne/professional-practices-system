package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso a datos de los practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class PractitionerDAO extends BaseDAO implements IPractitionerDAO {

    private static final String CURRENT_GROUP_SUBQUERY =
            "(SELECT ge.group_id FROM group_enrollment ge WHERE ge.practitioner_id = p.practitioner_id " +
                    "ORDER BY ge.enrollment_id DESC LIMIT 1) AS group_id ";
    private static final String SQL_INSERT_PRACTITIONER =
            "INSERT INTO practitioner (practitioner_id, indigenous_language, grade) VALUES (?, ?, ?)";
    private static final String SQL_UPDATE_PRACTITIONER =
            "UPDATE practitioner SET indigenous_language = ?, grade = ? WHERE practitioner_id = ?";
    private static final String SQL_SELECT_PRACTITIONER_BY_ID =
            "SELECT u.user_id, u.username AS matricula, u.password, u.name, u.last_name, " +
                    "u.email, u.status, u.gender, p.indigenous_language, p.grade, " +
                    CURRENT_GROUP_SUBQUERY +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "WHERE p.practitioner_id = ?";
    private static final String SQL_SELECT_ALL_PRACTITIONERS =
            "SELECT u.user_id, u.username AS matricula, u.password, u.name, u.last_name, " +
                    "u.email, u.status, u.gender, p.indigenous_language, p.grade, " +
                    CURRENT_GROUP_SUBQUERY +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id";
    private static final String SQL_SELECT_PRACTITIONERS_PENDING_ASSIGNMENT =
            "SELECT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "WHERE p.practitioner_id IN (SELECT practitioner_id FROM project_postulation) " +
                    "AND p.practitioner_id NOT IN " +
                    "(SELECT practitioner_id FROM project_postulation WHERE postulation_status = 'Assigned')";
    private static final String SQL_SELECT_ASSIGNED_PRACTITIONERS =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN project_postulation pp ON p.practitioner_id = pp.practitioner_id " +
                    "WHERE u.status = 'Active' AND pp.postulation_status = 'Assigned'";
    private static final String SQL_SELECT_PRACTITIONERS_BY_PROFESSOR =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN project_postulation pp ON p.practitioner_id = pp.practitioner_id " +
                    "INNER JOIN group_enrollment ge ON p.practitioner_id = ge.practitioner_id " +
                    "INNER JOIN practice_group pg ON ge.group_id = pg.group_id " +
                    "WHERE u.status = 'Active' " +
                    "AND pp.postulation_status = 'Assigned' " +
                    "AND pg.professor_id = ?";
    private static final String SQL_SELECT_PRACTITIONERS_BY_PROFESSOR_AND_PERIOD =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN project_postulation pp ON p.practitioner_id = pp.practitioner_id " +
                    "INNER JOIN group_enrollment ge ON p.practitioner_id = ge.practitioner_id " +
                    "INNER JOIN practice_group pg ON ge.group_id = pg.group_id " +
                    "WHERE u.status = 'Active' " +
                    "AND pp.postulation_status = 'Assigned' " +
                    "AND pg.professor_id = ? " +
                    "AND pg.period_id = ?";
    private static final String SQL_SELECT_PRACTITIONERS_BY_GROUP =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN project_postulation pp ON p.practitioner_id = pp.practitioner_id " +
                    "INNER JOIN group_enrollment ge ON p.practitioner_id = ge.practitioner_id " +
                    "WHERE u.status = 'Active' " +
                    "AND pp.postulation_status = 'Assigned' " +
                    "AND ge.group_id = ?";
    private static final String SQL_SELECT_ENROLLED_PRACTITIONERS_BY_GROUP =
            "SELECT DISTINCT u.user_id, u.username AS matricula, u.name, u.last_name, u.email " +
                    "FROM practitioner p " +
                    "INNER JOIN user u ON p.practitioner_id = u.user_id " +
                    "INNER JOIN group_enrollment ge ON p.practitioner_id = ge.practitioner_id " +
                    "WHERE u.status = 'Active' " +
                    "AND ge.status = 'Active' " +
                    "AND ge.group_id = ?";

    private final IUserDAO userDAO;

    /**
     * Crea el DAO de practicantes con la fuente de conexiones y el DAO de usuarios.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     * @param userDAO            DAO de usuarios usado para los datos comunes de cuenta
     */
    @Inject
    public PractitionerDAO(IDatabaseConnection databaseConnection, IUserDAO userDAO) {
        super(databaseConnection);
        this.userDAO = userDAO;
    }

    /**
     * Inserta un practicante creando primero su usuario base dentro de una transacción.
     *
     * @param practitioner practicante con los datos a registrar
     * @return identificador generado para el practicante, o {@code -1} si la operación falla
     * @throws DAOException si ocurre un error y se revierte la transacción
     */
    @Override
    public int insertPractitioner(Practitioner practitioner) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                int generatedUserId = userDAO.insertUser(practitioner, connection);

                if (generatedUserId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PRACTITIONER)) {
                        statement.setInt(1, generatedUserId);
                        statement.setString(2, practitioner.getIndigenousLanguage());
                        statement.setDouble(3, practitioner.getGrade());

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
                throw new DAOException("Error SQL al insertar el practicante. Cambios revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }

        return generatedId;
    }

    /**
     * Recupera un practicante con sus datos completos y su grupo actual.
     *
     * @param practitionerId identificador del practicante a recuperar
     * @return practicante encontrado, o un {@link Practitioner} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
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
            throw new DAOException("Error en la base de datos al intentar recuperar el practicante.", e);
        }

        return recoveredPractitioner;
    }

    /**
     * Recupera todos los practicantes con sus datos completos y su grupo actual.
     *
     * @return lista con todos los practicantes; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Practitioner> getAllPractitioners() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_PRACTITIONERS, this::mapResultSetToPractitioner);
    }

    /**
     * Actualiza los datos de usuario y los datos propios de un practicante en una transacción.
     *
     * @param practitionerToUpdate practicante con los datos modificados
     * @param practitionerId       identificador del practicante a actualizar
     * @throws DAOException si ocurre un error y se revierte la transacción
     */
    @Override
    public void updatePractitioner(Practitioner practitionerToUpdate, int practitionerId) throws DAOException {
        practitionerToUpdate.setId(practitionerId);

        try (Connection connection = databaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                executeUpdateTransaction(connection, practitionerToUpdate, practitionerId);
                connection.commit();
            } catch (SQLException | DAOException e) {
                connection.rollback();
                throw new DAOException("Error SQL al actualizar el practicante. Cambios revertidos.", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DAOException("Error crítico de conexión a la base de datos.", e);
        }
    }

    /**
     * Recupera los practicantes que se han postulado pero aún no tienen proyecto asignado.
     *
     * @return lista de practicantes pendientes de asignación; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Practitioner> retrievePractitionersPendingAssignment() throws DAOException {
        return recoverALL(SQL_SELECT_PRACTITIONERS_PENDING_ASSIGNMENT, this::mapResultSetToMinimalPractitioner);
    }

    /**
     * Recupera los practicantes activos que ya tienen un proyecto asignado.
     *
     * @return lista de practicantes asignados; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Practitioner> retrieveAssignedPractitioners() throws DAOException {
        return recoverALL(SQL_SELECT_ASSIGNED_PRACTITIONERS, this::mapResultSetToMinimalPractitioner);
    }

    /**
     * Recupera los practicantes asignados que pertenecen a los grupos de un profesor.
     *
     * @param professorId identificador del profesor
     * @return lista de practicantes a cargo del profesor; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Practitioner> retrievePractitionersByProfessor(int professorId) throws DAOException {
        return recoverALL(SQL_SELECT_PRACTITIONERS_BY_PROFESSOR, this::mapResultSetToMinimalPractitioner, professorId);
    }

    /**
     * Recupera los practicantes asignados a un profesor dentro de un periodo específico.
     *
     * @param professorId identificador del profesor
     * @param periodId    identificador del periodo escolar
     * @return lista de practicantes del profesor en ese periodo; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Practitioner> retrievePractitionersByProfessorAndPeriod(int professorId, int periodId)
            throws DAOException {
        return recoverALL(SQL_SELECT_PRACTITIONERS_BY_PROFESSOR_AND_PERIOD, this::mapResultSetToMinimalPractitioner,
                professorId, periodId);
    }

    /**
     * Recupera los practicantes activos y asignados que pertenecen a un grupo.
     *
     * @param groupId identificador del grupo de prácticas
     * @return lista de practicantes asignados del grupo; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Practitioner> retrievePractitionersByGroup(int groupId) throws DAOException {
        return recoverALL(SQL_SELECT_PRACTITIONERS_BY_GROUP, this::mapResultSetToMinimalPractitioner, groupId);
    }

    /**
     * Recupera los practicantes con inscripción activa en un grupo, estén o no asignados.
     *
     * @param groupId identificador del grupo de prácticas
     * @return lista de practicantes inscritos en el grupo; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Practitioner> retrieveEnrolledPractitionersByGroup(int groupId) throws DAOException {
        return recoverALL(SQL_SELECT_ENROLLED_PRACTITIONERS_BY_GROUP, this::mapResultSetToMinimalPractitioner, groupId);
    }

    /**
     * Actualiza, sobre una conexión transaccional, el usuario y los datos propios del practicante.
     *
     * @param connection     conexión transaccional sobre la que se ejecuta la actualización
     * @param practitioner   practicante con los datos modificados
     * @param practitionerId identificador del practicante a actualizar
     * @throws SQLException si ocurre un error al ejecutar las sentencias
     * @throws DAOException si falla la actualización del usuario asociado
     */
    private void executeUpdateTransaction(Connection connection, Practitioner practitioner, int practitionerId)
            throws SQLException, DAOException {
        userDAO.updateUser(practitioner, connection);

        try (PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PRACTITIONER)) {
            statement.setString(1, practitioner.getIndigenousLanguage());
            statement.setDouble(2, practitioner.getGrade());
            statement.setInt(3, practitionerId);
            statement.executeUpdate();
        }
    }

    /**
     * Construye un practicante con sus datos mínimos de identificación a partir de la fila actual.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return practicante con sus datos básicos
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Practitioner mapResultSetToMinimalPractitioner(ResultSet resultSet) throws SQLException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(resultSet.getInt("user_id"));
        practitioner.setEnrollment(resultSet.getString("matricula"));
        practitioner.setUserName(resultSet.getString("matricula"));
        practitioner.setName(resultSet.getString("name"));
        practitioner.setLastName(resultSet.getString("last_name"));
        practitioner.setEmail(resultSet.getString("email"));
        return practitioner;
    }

    /**
     * Construye un practicante con todos sus datos a partir de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return practicante con sus datos completos
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Practitioner mapResultSetToPractitioner(ResultSet resultSet) throws SQLException {
        Practitioner practitioner = new Practitioner();
        practitioner.setId(resultSet.getInt("user_id"));
        practitioner.setPassword(resultSet.getString("password"));
        practitioner.setName(resultSet.getString("name"));
        practitioner.setLastName(resultSet.getString("last_name"));
        practitioner.setEmail(resultSet.getString("email"));
        practitioner.setStatus(resolveNullableStatus(resultSet));
        practitioner.setGender(resolveNullableGender(resultSet));
        practitioner.setEnrollment(resultSet.getString("matricula"));
        practitioner.setUserName(resultSet.getString("matricula"));
        practitioner.setIndigenousLanguage(resultSet.getString("indigenous_language"));
        practitioner.setGrade(resultSet.getDouble("grade"));
        practitioner.setGroupId(resolveNullableGroupId(resultSet));
        return practitioner;
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
     * Obtiene el identificador del grupo actual tolerando valores nulos en la columna.
     *
     * @param resultSet resultado posicionado en la fila a leer
     * @return identificador del grupo, o {@code null} si el practicante no tiene grupo
     * @throws SQLException si ocurre un error al leer la columna
     */
    private Integer resolveNullableGroupId(ResultSet resultSet) throws SQLException {
        int groupId = resultSet.getInt("group_id");
        return resultSet.wasNull() ? null : groupId;
    }
}