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
            "INSERT INTO group_enrollment (practitioner_id, group_id, period_id, status) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_ENROLLMENT_BY_PRACTITIONER_AND_PERIOD =
            "SELECT enrollment_id, practitioner_id, group_id, period_id, status " +
                    "FROM group_enrollment WHERE practitioner_id = ? AND period_id = ?";
    private static final String SQL_SELECT_ENROLLMENTS_BY_PRACTITIONER =
            "SELECT enrollment_id, practitioner_id, group_id, period_id, status " +
                    "FROM group_enrollment WHERE practitioner_id = ?";
    private static final String SQL_SELECT_ENROLLMENTS_BY_GROUP =
            "SELECT enrollment_id, practitioner_id, group_id, period_id, status " +
                    "FROM group_enrollment WHERE group_id = ?";

    @Inject
    public GroupEnrollmentDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertEnrollment(GroupEnrollment enrollment) throws DAOException {
        return insertTuple(SQL_INSERT_ENROLLMENT, statement -> {
            statement.setInt(1, enrollment.getPractitionerId());
            statement.setInt(2, enrollment.getGroupId());
            statement.setInt(3, enrollment.getPeriodId());
            statement.setString(4, enrollment.getStatus().getDatabaseValue());
        });
    }

    @Override
    public GroupEnrollment recoverEnrollmentByPractitionerAndPeriod(int practitionerId, int periodId)
            throws DAOException {
        List<GroupEnrollment> enrollments = recoverALL(SQL_SELECT_ENROLLMENT_BY_PRACTITIONER_AND_PERIOD,
                this::mapResultSetToEnrollment, practitionerId, periodId);

        return enrollments.isEmpty() ? null : enrollments.get(0);
    }

    @Override
    public List<GroupEnrollment> getEnrollmentsByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_ENROLLMENTS_BY_PRACTITIONER, this::mapResultSetToEnrollment, practitionerId);
    }

    @Override
    public List<GroupEnrollment> getEnrollmentsByGroup(int groupId) throws DAOException {
        return recoverALL(SQL_SELECT_ENROLLMENTS_BY_GROUP, this::mapResultSetToEnrollment, groupId);
    }

    private GroupEnrollment mapResultSetToEnrollment(ResultSet resultSet) throws SQLException {
        GroupEnrollment enrollment = new GroupEnrollment();
        enrollment.setEnrollmentId(resultSet.getInt("enrollment_id"));
        enrollment.setPractitionerId(resultSet.getInt("practitioner_id"));
        enrollment.setGroupId(resultSet.getInt("group_id"));
        enrollment.setPeriodId(resultSet.getInt("period_id"));
        enrollment.setStatus(EnrollmentStatus.fromString(resultSet.getString("status")));

        return enrollment;
    }
}
