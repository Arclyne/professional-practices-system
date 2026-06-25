package mx.uv.fei.domain.manager.academic;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IGroupEnrollmentDAO;
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.domain.dto.GroupEnrollment;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.enums.EnrollmentStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.evaluation.GradingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class PeriodConclusionManager {

    private static final Logger log = LoggerFactory.getLogger(PeriodConclusionManager.class);

    private static final double AUTOMATIC_FINAL_GRADE = 5.0;

    private final IPeriodDAO periodDAO;
    private final IGroupEnrollmentDAO groupEnrollmentDAO;
    private final IPracticeGroupDAO practiceGroupDAO;
    private final GradingManager gradingManager;

    @Inject
    public PeriodConclusionManager(IPeriodDAO periodDAO, IGroupEnrollmentDAO groupEnrollmentDAO,
                                   IPracticeGroupDAO practiceGroupDAO, GradingManager gradingManager) {
        this.periodDAO = periodDAO;
        this.groupEnrollmentDAO = groupEnrollmentDAO;
        this.practiceGroupDAO = practiceGroupDAO;
        this.gradingManager = gradingManager;
    }

    public void concludePeriod(int periodId) throws ManagerException {
        try {
            Period period = periodDAO.recoverPeriod(periodId);
            assignAutomaticGradesToUngraded(periodId, period.getPeriodName());
            periodDAO.deactivatePeriod(periodId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo concluir el periodo académico.", e);
        }
    }

    private void assignAutomaticGradesToUngraded(int periodId, String periodName)
            throws ManagerException, DAOException {
        List<GroupEnrollment> periodEnrollments = groupEnrollmentDAO.getEnrollmentsByPeriod(periodId);
        for (GroupEnrollment enrollment : periodEnrollments) {
            assignAutomaticGradeIfMissing(enrollment, periodName);
        }
    }

    private void assignAutomaticGradeIfMissing(GroupEnrollment enrollment, String periodName)
            throws ManagerException, DAOException {
        boolean isActiveEnrollment = enrollment.getStatus() == EnrollmentStatus.ACTIVE;
        boolean isUngraded = gradingManager.getGradeByPractitionerAndPeriod(
                enrollment.getPractitionerId(), periodName) == null;
        if (isActiveEnrollment && isUngraded) {
            int professorId = practiceGroupDAO.recoverPracticeGroup(enrollment.getGroupId()).getProfessorId();
            gradingManager.registerGrade(enrollment.getPractitionerId(), professorId, periodName,
                    AUTOMATIC_FINAL_GRADE);
        }
    }
}
