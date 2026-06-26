package mx.uv.fei.domain.manager.academic;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IGroupEnrollmentDAO;
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.dataaccess.interfaces.IProfessorDAO;
import mx.uv.fei.domain.dto.GroupEnrollment;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.EnrollmentStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.evaluation.GradingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class PeriodConclusionManager {

    private static final Logger log = LoggerFactory.getLogger(PeriodConclusionManager.class);

    private static final String UNGRADED_HEADER =
            "No es posible concluir el periodo: aún hay practicantes sin calificar.";
    private static final String UNGRADED_FOOTER =
            "Pide a los profesores indicados que registren las calificaciones pendientes antes de concluir el periodo.";
    private static final String PROFESSOR_PREFIX = "Profesor ";
    private static final String PRACTITIONER_BULLET = "  • ";

    private final IPeriodDAO periodDAO;
    private final IGroupEnrollmentDAO groupEnrollmentDAO;
    private final IPracticeGroupDAO practiceGroupDAO;
    private final IPractitionerDAO practitionerDAO;
    private final IProfessorDAO professorDAO;
    private final GradingManager gradingManager;

    @Inject
    public PeriodConclusionManager(IPeriodDAO periodDAO, IGroupEnrollmentDAO groupEnrollmentDAO,
                                   IPracticeGroupDAO practiceGroupDAO, IPractitionerDAO practitionerDAO,
                                   IProfessorDAO professorDAO, GradingManager gradingManager) {
        this.periodDAO = periodDAO;
        this.groupEnrollmentDAO = groupEnrollmentDAO;
        this.practiceGroupDAO = practiceGroupDAO;
        this.practitionerDAO = practitionerDAO;
        this.professorDAO = professorDAO;
        this.gradingManager = gradingManager;
    }

    public void concludePeriod(int periodId) throws ManagerException {
        try {
            Period period = periodDAO.recoverPeriod(periodId);
            List<GroupEnrollment> ungradedEnrollments = collectUngradedEnrollments(periodId, period.getPeriodName());
            if (!ungradedEnrollments.isEmpty()) {
                throw new ManagerException(buildUngradedMessage(ungradedEnrollments));
            }
            periodDAO.deactivatePeriod(periodId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo concluir el periodo académico.", e);
        }
    }

    private List<GroupEnrollment> collectUngradedEnrollments(int periodId, String periodName)
            throws ManagerException, DAOException {
        List<GroupEnrollment> ungradedEnrollments = new ArrayList<>();
        for (GroupEnrollment enrollment : groupEnrollmentDAO.getEnrollmentsByPeriod(periodId)) {
            if (isActiveAndUngraded(enrollment, periodName)) {
                ungradedEnrollments.add(enrollment);
            }
        }
        return ungradedEnrollments;
    }

    private boolean isActiveAndUngraded(GroupEnrollment enrollment, String periodName) throws ManagerException {
        boolean isActiveEnrollment = enrollment.getStatus() == EnrollmentStatus.ACTIVE;
        return isActiveEnrollment && gradingManager.getGradeByPractitionerAndPeriod(
                enrollment.getPractitionerId(), periodName) == null;
    }

    private String buildUngradedMessage(List<GroupEnrollment> ungradedEnrollments) throws DAOException {
        Map<String, List<String>> practitionersByProfessor = groupByProfessor(ungradedEnrollments);
        StringBuilder message = new StringBuilder(UNGRADED_HEADER);
        for (Map.Entry<String, List<String>> professorEntry : practitionersByProfessor.entrySet()) {
            message.append(System.lineSeparator()).append(System.lineSeparator())
                    .append(professorEntry.getKey()).append(":");
            appendPractitioners(message, professorEntry.getValue());
        }
        message.append(System.lineSeparator()).append(System.lineSeparator()).append(UNGRADED_FOOTER);
        return message.toString();
    }

    private void appendPractitioners(StringBuilder message, List<String> practitionerLabels) {
        for (String practitionerLabel : practitionerLabels) {
            message.append(System.lineSeparator()).append(PRACTITIONER_BULLET).append(practitionerLabel);
        }
    }

    private Map<String, List<String>> groupByProfessor(List<GroupEnrollment> ungradedEnrollments) throws DAOException {
        Map<String, List<String>> practitionersByProfessor = new LinkedHashMap<>();
        for (GroupEnrollment enrollment : ungradedEnrollments) {
            String professorLabel = resolveProfessorLabel(enrollment.getGroupId());
            practitionersByProfessor.computeIfAbsent(professorLabel, _ -> new ArrayList<>())
                    .add(resolvePractitionerLabel(enrollment.getPractitionerId()));
        }
        return practitionersByProfessor;
    }

    private String resolveProfessorLabel(int groupId) throws DAOException {
        PracticeGroup group = practiceGroupDAO.recoverPracticeGroup(groupId);
        Professor professor = professorDAO.recoverProfessor(group.getProfessorId());
        return PROFESSOR_PREFIX + fullName(professor.getName(), professor.getLastName());
    }

    private String resolvePractitionerLabel(int practitionerId) throws DAOException {
        Practitioner practitioner = practitionerDAO.recoverPractitioner(practitionerId);
        String practitionerName = fullName(practitioner.getName(), practitioner.getLastName());
        String enrollment = practitioner.getEnrollment();
        return enrollment != null && !enrollment.isBlank()
                ? practitionerName + " (" + enrollment + ")"
                : practitionerName;
    }

    private String fullName(String name, String lastName) {
        return ((name != null ? name : "") + " " + (lastName != null ? lastName : "")).trim();
    }
}
