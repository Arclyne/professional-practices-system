package mx.uv.fei.domain.manager.academic;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IGroupEnrollmentDAO;
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
import mx.uv.fei.dataaccess.interfaces.IPractitionerGradeDAO;
import mx.uv.fei.domain.dto.GroupEnrollment;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeStatus;
import mx.uv.fei.domain.dto.PractitionerGrade;
import mx.uv.fei.domain.enums.PeriodStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Component
public class PracticeAccessManager {

    private static final Logger log = LoggerFactory.getLogger(PracticeAccessManager.class);

    private static final int SECOND_OPPORTUNITY = 2;
    private static final String PERIOD_CONCLUDED_MESSAGE =
            "El periodo de prácticas ha concluido. Ya no puedes realizar entregas.";
    private static final String PRACTICES_FINISHED_MESSAGE =
            "Has concluido tus prácticas profesionales. Ya no puedes realizar entregas.";
    private static final String SECOND_OPPORTUNITY_BANNER =
            "Estás inscrito en segunda oportunidad.";
    private static final String FINAL_GRADE_SUFFIX_FORMAT = " Tu calificación final es %.1f / 10.";

    private final IGroupEnrollmentDAO groupEnrollmentDAO;
    private final IPeriodDAO periodDAO;
    private final IPractitionerGradeDAO practitionerGradeDAO;

    @Inject
    public PracticeAccessManager(IGroupEnrollmentDAO groupEnrollmentDAO, IPeriodDAO periodDAO,
                                 IPractitionerGradeDAO practitionerGradeDAO) {
        this.groupEnrollmentDAO = groupEnrollmentDAO;
        this.periodDAO = periodDAO;
        this.practitionerGradeDAO = practitionerGradeDAO;
    }

    public PracticeStatus resolveStatus(int practitionerId) throws ManagerException {
        try {
            GroupEnrollment latestEnrollment = groupEnrollmentDAO.recoverLatestEnrollment(practitionerId);
            return latestEnrollment == null ? buildNotEnrolledStatus() : buildStatus(practitionerId, latestEnrollment);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo determinar el estado de prácticas del practicante.", e);
        }
    }

    public void ensureSubmissionsAllowed(int practitionerId) throws ManagerException {
        PracticeStatus status = resolveStatus(practitionerId);
        String blockingMessage = resolveBlockingMessage(status);
        if (blockingMessage != null) {
            throw new ManagerException(blockingMessage);
        }
    }

    public String buildHomeStatusMessage(PracticeStatus status) {
        List<String> bannerLines = new ArrayList<>();
        if (status.periodConcluded()) {
            bannerLines.add(appendFinalGrade(PERIOD_CONCLUDED_MESSAGE, status.finalGrade()));
        } else if (status.finished()) {
            bannerLines.add(appendFinalGrade(PRACTICES_FINISHED_MESSAGE, status.finalGrade()));
        }
        if (status.secondOpportunity()) {
            bannerLines.add(SECOND_OPPORTUNITY_BANNER);
        }
        return String.join(System.lineSeparator(), bannerLines);
    }

    private PracticeStatus buildStatus(int practitionerId, GroupEnrollment latestEnrollment) throws DAOException {
        Period period = periodDAO.recoverPeriod(latestEnrollment.getPeriodId());
        boolean periodConcluded = isPeriodConcluded(period);
        Double finalGrade = resolveFinalGrade(practitionerId, period);
        boolean finished = finalGrade != null;
        int opportunityNumber = latestEnrollment.getOpportunityNumber();
        boolean secondOpportunity = opportunityNumber >= SECOND_OPPORTUNITY;

        return new PracticeStatus(true, periodConcluded, finished, finalGrade, opportunityNumber, secondOpportunity);
    }

    private Double resolveFinalGrade(int practitionerId, Period period) throws DAOException {
        Double finalGrade = null;
        if (period != null && period.getPeriodName() != null) {
            PractitionerGrade grade = practitionerGradeDAO.getGradeByPractitionerAndPeriod(
                    practitionerId, period.getPeriodName());
            finalGrade = grade != null ? grade.getFinalGrade() : null;
        }
        return finalGrade;
    }

    private boolean isPeriodConcluded(Period period) {
        return period != null && period.getPeriodStatus() != null
                && PeriodStatus.CONCLUDED.getDatabaseValue().equalsIgnoreCase(period.getPeriodStatus());
    }

    private String resolveBlockingMessage(PracticeStatus status) {
        String message = null;
        if (status.periodConcluded()) {
            message = PERIOD_CONCLUDED_MESSAGE;
        } else if (status.finished()) {
            message = PRACTICES_FINISHED_MESSAGE;
        }
        return message;
    }

    private String appendFinalGrade(String bannerMessage, Double finalGrade) {
        return finalGrade != null
                ? bannerMessage + String.format(FINAL_GRADE_SUFFIX_FORMAT, finalGrade)
                : bannerMessage;
    }

    private PracticeStatus buildNotEnrolledStatus() {
        return new PracticeStatus(false, false, false, null, 0, false);
    }
}
