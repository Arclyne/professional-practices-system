package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.GroupEnrollment;

import java.util.List;

public interface IGroupEnrollmentDAO {

    int insertEnrollment(GroupEnrollment enrollment) throws DAOException;

    GroupEnrollment recoverEnrollmentByPractitionerAndPeriod(int practitionerId, int periodId) throws DAOException;

    GroupEnrollment recoverLatestEnrollment(int practitionerId) throws DAOException;

    List<GroupEnrollment> getEnrollmentsByPractitioner(int practitionerId) throws DAOException;

    List<GroupEnrollment> getEnrollmentsByGroup(int groupId) throws DAOException;

    List<GroupEnrollment> getEnrollmentsByPeriod(int periodId) throws DAOException;
}
