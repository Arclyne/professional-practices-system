package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IGroupEnrollmentDAO;
import mx.uv.fei.domain.dto.GroupEnrollment;
import mx.uv.fei.domain.enums.EnrollmentStatus;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Acceso a datos de las inscripciones de practicantes en grupos de práctica.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class GroupEnrollmentDAO extends BaseDAO implements IGroupEnrollmentDAO {

    private static final String SQL_INSERT_ENROLLMENT =
            "INSERT INTO group_enrollment (practitioner_id, group_id, period_id, opportunity_number, status) " +
                    "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_SELECT_ENROLLMENT_BY_PRACTITIONER_AND_PERIOD =
            "SELECT enrollment_id, practitioner_id, group_id, period_id, opportunity_number, status " +
                    "FROM group_enrollment WHERE practitioner_id = ? AND period_id = ?";
    private static final String SQL_SELECT_LATEST_ENROLLMENT_BY_PRACTITIONER =
            "SELECT enrollment_id, practitioner_id, group_id, period_id, opportunity_number, status " +
                    "FROM group_enrollment WHERE practitioner_id = ? ORDER BY enrollment_id DESC LIMIT 1";
    private static final String SQL_SELECT_ENROLLMENTS_BY_PRACTITIONER =
            "SELECT enrollment_id, practitioner_id, group_id, period_id, opportunity_number, status " +
                    "FROM group_enrollment WHERE practitioner_id = ?";
    private static final String SQL_SELECT_ENROLLMENTS_BY_GROUP =
            "SELECT enrollment_id, practitioner_id, group_id, period_id, opportunity_number, status " +
                    "FROM group_enrollment WHERE group_id = ?";
    private static final String SQL_SELECT_ENROLLMENTS_BY_PERIOD =
            "SELECT enrollment_id, practitioner_id, group_id, period_id, opportunity_number, status " +
                    "FROM group_enrollment WHERE period_id = ?";

    /**
     * Crea el DAO de inscripciones a grupos con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public GroupEnrollmentDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta una inscripción de un practicante a un grupo y devuelve su identificador generado.
     *
     * @param enrollment inscripción con los datos a registrar
     * @return identificador generado para la inscripción, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar la inscripción
     */
    @Override
    public int insertEnrollment(GroupEnrollment enrollment) throws DAOException {
        return insertTuple(SQL_INSERT_ENROLLMENT, statement -> {
            statement.setInt(1, enrollment.getPractitionerId());
            statement.setInt(2, enrollment.getGroupId());
            statement.setInt(3, enrollment.getPeriodId());
            statement.setInt(4, enrollment.getOpportunityNumber());
            statement.setString(5, enrollment.getStatus().getDatabaseValue());
        });
    }

    /**
     * Recupera la inscripción de un practicante en un periodo determinado.
     *
     * @param practitionerId identificador del practicante
     * @param periodId       identificador del periodo escolar
     * @return inscripción encontrada, o {@code null} si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public GroupEnrollment recoverEnrollmentByPractitionerAndPeriod(int practitionerId, int periodId)
            throws DAOException {
        List<GroupEnrollment> enrollments = recoverALL(SQL_SELECT_ENROLLMENT_BY_PRACTITIONER_AND_PERIOD,
                this::mapResultSetToEnrollment, practitionerId, periodId);

        return enrollments.isEmpty() ? null : enrollments.get(0);
    }

    /**
     * Recupera la inscripción más reciente de un practicante.
     *
     * @param practitionerId identificador del practicante
     * @return inscripción más reciente, o {@code null} si no tiene ninguna
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public GroupEnrollment recoverLatestEnrollment(int practitionerId) throws DAOException {
        List<GroupEnrollment> enrollments = recoverALL(SQL_SELECT_LATEST_ENROLLMENT_BY_PRACTITIONER,
                this::mapResultSetToEnrollment, practitionerId);

        return enrollments.isEmpty() ? null : enrollments.get(0);
    }

    /**
     * Recupera todas las inscripciones de un practicante.
     *
     * @param practitionerId identificador del practicante
     * @return lista de inscripciones del practicante; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<GroupEnrollment> getEnrollmentsByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_ENROLLMENTS_BY_PRACTITIONER, this::mapResultSetToEnrollment, practitionerId);
    }

    /**
     * Recupera todas las inscripciones de un grupo.
     *
     * @param groupId identificador del grupo de prácticas
     * @return lista de inscripciones del grupo; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<GroupEnrollment> getEnrollmentsByGroup(int groupId) throws DAOException {
        return recoverALL(SQL_SELECT_ENROLLMENTS_BY_GROUP, this::mapResultSetToEnrollment, groupId);
    }

    /**
     * Recupera todas las inscripciones de un periodo escolar.
     *
     * @param periodId identificador del periodo escolar
     * @return lista de inscripciones del periodo; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<GroupEnrollment> getEnrollmentsByPeriod(int periodId) throws DAOException {
        return recoverALL(SQL_SELECT_ENROLLMENTS_BY_PERIOD, this::mapResultSetToEnrollment, periodId);
    }

    /**
     * Construye una inscripción con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return inscripción con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private GroupEnrollment mapResultSetToEnrollment(ResultSet resultSet) throws SQLException {
        GroupEnrollment enrollment = new GroupEnrollment();
        enrollment.setEnrollmentId(resultSet.getInt("enrollment_id"));
        enrollment.setPractitionerId(resultSet.getInt("practitioner_id"));
        enrollment.setGroupId(resultSet.getInt("group_id"));
        enrollment.setPeriodId(resultSet.getInt("period_id"));
        enrollment.setOpportunityNumber(resultSet.getInt("opportunity_number"));
        enrollment.setStatus(EnrollmentStatus.fromString(resultSet.getString("status")));

        return enrollment;
    }
}
