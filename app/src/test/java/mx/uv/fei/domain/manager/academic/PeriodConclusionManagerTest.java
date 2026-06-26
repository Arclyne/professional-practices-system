package mx.uv.fei.domain.manager.academic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import mx.uv.fei.dataaccess.interfaces.IGroupEnrollmentDAO;
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.dataaccess.interfaces.IProfessorDAO;
import mx.uv.fei.domain.dto.GroupEnrollment;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.PractitionerGrade;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.EnrollmentStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.evaluation.GradingManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PeriodConclusionManagerTest {

    private static final int PERIOD_ID = 7;
    private static final String PERIOD_NAME = "Febrero - Julio 2026";
    private static final int GROUP_ID = 3;
    private static final int PROFESSOR_ID = 42;
    private static final int GRADED_PRACTITIONER_ID = 100;
    private static final int UNGRADED_PRACTITIONER_ID = 200;

    private IPeriodDAO periodDAO;
    private IGroupEnrollmentDAO groupEnrollmentDAO;
    private IPracticeGroupDAO practiceGroupDAO;
    private IPractitionerDAO practitionerDAO;
    private IProfessorDAO professorDAO;
    private GradingManager gradingManager;
    private PeriodConclusionManager periodConclusionManager;

    @BeforeEach
    void setUp() throws Exception {
        periodDAO = mock(IPeriodDAO.class);
        groupEnrollmentDAO = mock(IGroupEnrollmentDAO.class);
        practiceGroupDAO = mock(IPracticeGroupDAO.class);
        practitionerDAO = mock(IPractitionerDAO.class);
        professorDAO = mock(IProfessorDAO.class);
        gradingManager = mock(GradingManager.class);
        periodConclusionManager = new PeriodConclusionManager(periodDAO, groupEnrollmentDAO, practiceGroupDAO,
                practitionerDAO, professorDAO, gradingManager);

        when(periodDAO.recoverPeriod(PERIOD_ID)).thenReturn(buildPeriod());
        when(practiceGroupDAO.recoverPracticeGroup(GROUP_ID)).thenReturn(buildPracticeGroup());
        when(professorDAO.recoverProfessor(PROFESSOR_ID)).thenReturn(buildProfessor());
        when(practitionerDAO.recoverPractitioner(UNGRADED_PRACTITIONER_ID)).thenReturn(buildPractitioner());
    }

    private Period buildPeriod() {
        Period period = new Period();
        period.setPeriodId(PERIOD_ID);
        period.setPeriodName(PERIOD_NAME);
        return period;
    }

    private PracticeGroup buildPracticeGroup() {
        PracticeGroup group = new PracticeGroup();
        group.setGroupId(GROUP_ID);
        group.setProfessorId(PROFESSOR_ID);
        return group;
    }

    private Professor buildProfessor() {
        Professor professor = new Professor();
        professor.setName("María");
        professor.setLastName("Ruiz");
        return professor;
    }

    private Practitioner buildPractitioner() {
        Practitioner practitioner = new Practitioner();
        practitioner.setName("Ana");
        practitioner.setLastName("López");
        practitioner.setEnrollment("zS20001");
        return practitioner;
    }

    private GroupEnrollment buildEnrollment(int practitionerId, EnrollmentStatus status) {
        GroupEnrollment enrollment = new GroupEnrollment();
        enrollment.setPractitionerId(practitionerId);
        enrollment.setGroupId(GROUP_ID);
        enrollment.setPeriodId(PERIOD_ID);
        enrollment.setStatus(status);
        return enrollment;
    }

    private void stubGrade(int practitionerId, PractitionerGrade grade) throws ManagerException {
        when(gradingManager.getGradeByPractitionerAndPeriod(practitionerId, PERIOD_NAME)).thenReturn(grade);
    }

    @Test
    void concludePeriod_AllPractitionersGraded_DeactivatesPeriod() throws Exception {
        when(groupEnrollmentDAO.getEnrollmentsByPeriod(PERIOD_ID))
                .thenReturn(List.of(buildEnrollment(GRADED_PRACTITIONER_ID, EnrollmentStatus.ACTIVE)));
        stubGrade(GRADED_PRACTITIONER_ID, new PractitionerGrade());

        periodConclusionManager.concludePeriod(PERIOD_ID);

        verify(periodDAO).deactivatePeriod(PERIOD_ID);
    }

    @Test
    void concludePeriod_AllPractitionersGraded_DoesNotThrow() throws Exception {
        when(groupEnrollmentDAO.getEnrollmentsByPeriod(PERIOD_ID))
                .thenReturn(List.of(buildEnrollment(GRADED_PRACTITIONER_ID, EnrollmentStatus.ACTIVE)));
        stubGrade(GRADED_PRACTITIONER_ID, new PractitionerGrade());

        assertDoesNotThrow(() -> periodConclusionManager.concludePeriod(PERIOD_ID));
    }

    @Test
    void concludePeriod_HasUngradedPractitioner_ThrowsManagerException() throws Exception {
        when(groupEnrollmentDAO.getEnrollmentsByPeriod(PERIOD_ID))
                .thenReturn(List.of(buildEnrollment(UNGRADED_PRACTITIONER_ID, EnrollmentStatus.ACTIVE)));
        stubGrade(UNGRADED_PRACTITIONER_ID, null);

        assertThrows(ManagerException.class, () -> periodConclusionManager.concludePeriod(PERIOD_ID));
    }

    @Test
    void concludePeriod_HasUngradedPractitioner_DoesNotDeactivatePeriod() throws Exception {
        when(groupEnrollmentDAO.getEnrollmentsByPeriod(PERIOD_ID))
                .thenReturn(List.of(buildEnrollment(UNGRADED_PRACTITIONER_ID, EnrollmentStatus.ACTIVE)));
        stubGrade(UNGRADED_PRACTITIONER_ID, null);

        assertThrows(ManagerException.class, () -> periodConclusionManager.concludePeriod(PERIOD_ID));

        verify(periodDAO, never()).deactivatePeriod(anyInt());
    }

    @Test
    void concludePeriod_HasUngradedPractitioner_DoesNotAssignAutomaticGrade() throws Exception {
        when(groupEnrollmentDAO.getEnrollmentsByPeriod(PERIOD_ID))
                .thenReturn(List.of(buildEnrollment(UNGRADED_PRACTITIONER_ID, EnrollmentStatus.ACTIVE)));
        stubGrade(UNGRADED_PRACTITIONER_ID, null);

        assertThrows(ManagerException.class, () -> periodConclusionManager.concludePeriod(PERIOD_ID));

        verify(gradingManager, never()).registerGrade(anyInt(), anyInt(), eq(PERIOD_NAME), anyDouble());
    }

    @Test
    void concludePeriod_HasUngradedPractitioner_MessageNamesProfessorAndPractitioner() throws Exception {
        when(groupEnrollmentDAO.getEnrollmentsByPeriod(PERIOD_ID))
                .thenReturn(List.of(buildEnrollment(UNGRADED_PRACTITIONER_ID, EnrollmentStatus.ACTIVE)));
        stubGrade(UNGRADED_PRACTITIONER_ID, null);

        ManagerException exception = assertThrows(ManagerException.class,
                () -> periodConclusionManager.concludePeriod(PERIOD_ID));

        assertTrue(exception.getMessage().contains("María Ruiz")
                && exception.getMessage().contains("Ana López"));
    }

    @Test
    void concludePeriod_InactiveUngradedEnrollment_IsIgnoredAndDeactivates() throws Exception {
        when(groupEnrollmentDAO.getEnrollmentsByPeriod(PERIOD_ID))
                .thenReturn(List.of(buildEnrollment(UNGRADED_PRACTITIONER_ID, EnrollmentStatus.INACTIVE)));

        periodConclusionManager.concludePeriod(PERIOD_ID);

        verify(periodDAO).deactivatePeriod(PERIOD_ID);
    }
}
