package mx.uv.fei.domain.manager.academic;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IGroupEnrollmentDAO;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.domain.common.PersistenceErrorTranslator;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.dto.GroupEnrollment;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.enums.EnrollmentStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class GroupEnrollmentManager {

    private static final Logger log = LoggerFactory.getLogger(GroupEnrollmentManager.class);

    private static final String ALREADY_ENROLLED_MESSAGE =
            "El practicante ya está inscrito en un grupo durante este periodo.";
    private static final String SECOND_OPPORTUNITY_EXHAUSTED_MESSAGE =
            "No puedes reinscribir a este practicante: ya agotó sus dos oportunidades de inscripción "
                    + "(segunda oportunidad).";
    private static final int MAX_ENROLLMENT_OPPORTUNITIES = 2;

    private final IGroupEnrollmentDAO groupEnrollmentDAO;
    private final IPracticeGroupDAO practiceGroupDAO;

    @Inject
    public GroupEnrollmentManager(IGroupEnrollmentDAO groupEnrollmentDAO, IPracticeGroupDAO practiceGroupDAO) {
        this.groupEnrollmentDAO = groupEnrollmentDAO;
        this.practiceGroupDAO = practiceGroupDAO;
    }

    public int enrollPractitionerInGroup(int practitionerId, int groupId) throws ManagerException {
        GroupEnrollment enrollment = new GroupEnrollment();
        enrollment.setPractitionerId(practitionerId);
        enrollment.setGroupId(groupId);
        enrollment.setPeriodId(resolvePeriodForGroup(groupId));

        return enrollPractitioner(enrollment);
    }

    public int enrollPractitioner(GroupEnrollment enrollment) throws ManagerException {
        validateEnrollmentData(enrollment);
        ensurePractitionerIsNotEnrolledInPeriod(enrollment.getPractitionerId(), enrollment.getPeriodId());
        enrollment.setOpportunityNumber(resolveOpportunityNumber(enrollment.getPractitionerId()));

        int generatedId;
        try {
            if (enrollment.getStatus() == null) {
                enrollment.setStatus(EnrollmentStatus.ACTIVE);
            }
            generatedId = groupEnrollmentDAO.insertEnrollment(enrollment);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw PersistenceErrorTranslator.translate(e);
        }

        if (generatedId <= 0) {
            throw new ManagerException("No se pudo inscribir al practicante en el grupo de prácticas.");
        }

        return generatedId;
    }

    public List<GroupEnrollment> getEnrollmentsByGroup(int groupId) throws ManagerException {
        try {
            return groupEnrollmentDAO.getEnrollmentsByGroup(groupId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al recuperar las inscripciones del grupo.", e);
        }
    }

    public List<GroupEnrollment> getEnrollmentsByPractitioner(int practitionerId) throws ManagerException {
        try {
            return groupEnrollmentDAO.getEnrollmentsByPractitioner(practitionerId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al recuperar las inscripciones del practicante.", e);
        }
    }

    private int resolvePeriodForGroup(int groupId) throws ManagerException {
        try {
            PracticeGroup practiceGroup = practiceGroupDAO.recoverPracticeGroup(groupId);
            if (practiceGroup.getGroupId() <= 0) {
                throw new ManagerException("El grupo de prácticas seleccionado no existe.");
            }
            return practiceGroup.getPeriodId();
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo recuperar el periodo del grupo de prácticas.", e);
        }
    }

    private int resolveOpportunityNumber(int practitionerId) throws ManagerException {
        int nextOpportunity;
        try {
            int previousEnrollments = groupEnrollmentDAO.getEnrollmentsByPractitioner(practitionerId).size();
            if (previousEnrollments >= MAX_ENROLLMENT_OPPORTUNITIES) {
                throw new ManagerException(SECOND_OPPORTUNITY_EXHAUSTED_MESSAGE);
            }
            nextOpportunity = previousEnrollments + 1;
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo verificar las inscripciones previas del practicante.", e);
        }
        return nextOpportunity;
    }

    private void ensurePractitionerIsNotEnrolledInPeriod(int practitionerId, int periodId) throws ManagerException {
        try {
            if (groupEnrollmentDAO.recoverEnrollmentByPractitionerAndPeriod(practitionerId, periodId) != null) {
                throw new ManagerException(ALREADY_ENROLLED_MESSAGE);
            }
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo verificar la inscripción del practicante.", e);
        }
    }

    private void validateEnrollmentData(GroupEnrollment enrollment) throws ManagerException {
        BaseValidator.validateId(enrollment.getPractitionerId(), "Debe seleccionar un practicante válido.");
        BaseValidator.validateId(enrollment.getGroupId(), "Debe seleccionar el grupo de prácticas.");
        BaseValidator.validateId(enrollment.getPeriodId(), "Debe seleccionar el periodo escolar.");
    }
}
