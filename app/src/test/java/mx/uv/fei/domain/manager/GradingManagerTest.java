package mx.uv.fei.domain.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerGradeDAO;
import mx.uv.fei.domain.dto.PractitionerGrade;
import mx.uv.fei.domain.exceptions.ManagerException;

import static org.junit.jupiter.api.Assertions.*;

public class GradingManagerTest {

    private static final int    PRACTITIONER_ID  = 123;
    private static final int    PROFESSOR_ID     = 68;
    private static final String PERIOD           = "Junio-Diciembre 2026";
    private static final double VALID_GRADE      = 8.5;
    private static final double TENTATIVE_GRADE  = 7.8;

    private GradingManager gradingManager;
    private StubPractitionerGradeDAO stubDAO;

    @BeforeEach
    void setUp() {
        stubDAO = new StubPractitionerGradeDAO();
        gradingManager = new GradingManager(stubDAO);
    }



    @Test
    void registerGrade_ValidData_ReturnsGradeRecord() throws ManagerException {
        stubDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade result = gradingManager.registerGrade(
                PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE);

        assertNotNull(result);
    }

    @Test
    void registerGrade_ValidData_SetsPractitionerId() throws ManagerException {
        stubDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade result = gradingManager.registerGrade(
                PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE);

        assertEquals(PRACTITIONER_ID, result.getPractitionerId());
    }

    @Test
    void registerGrade_ValidData_SetsTentativeGrade() throws ManagerException {
        stubDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade result = gradingManager.registerGrade(
                PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE);

        assertEquals(TENTATIVE_GRADE, result.getTentativeGrade());
    }

    @Test
    void registerGrade_ValidData_SetsFinalGrade() throws ManagerException {
        stubDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade result = gradingManager.registerGrade(
                PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE);

        assertEquals(VALID_GRADE, result.getFinalGrade());
    }

    @Test
    void registerGrade_GradeZero_IsValidMinimum() throws ManagerException {
        stubDAO.setTentativeGrade(0.0);

        PractitionerGrade result = gradingManager.registerGrade(
                PRACTITIONER_ID, PROFESSOR_ID, PERIOD, 0.0);

        assertNotNull(result);
    }

    @Test
    void registerGrade_GradeTen_IsValidMaximum() throws ManagerException {
        stubDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade result = gradingManager.registerGrade(
                PRACTITIONER_ID, PROFESSOR_ID, PERIOD, 10.0);

        assertNotNull(result);
    }



    @Test
    void registerGrade_GradeAbove10_ThrowsManagerException() {
        ManagerException e = assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, 10.1));

        assertNotNull(e.getMessage());
    }

    @Test
    void registerGrade_NegativeGrade_ThrowsManagerException() {
        ManagerException e = assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, -0.1));

        assertNotNull(e.getMessage());
    }

    @Test
    void registerGrade_NullPeriod_ThrowsManagerException() {
        ManagerException e = assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, null, VALID_GRADE));

        assertNotNull(e.getMessage());
    }

    @Test
    void registerGrade_BlankPeriod_ThrowsManagerException() {
        ManagerException e = assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, "   ", VALID_GRADE));

        assertNotNull(e.getMessage());
    }

    @Test
    void registerGrade_AlreadyGraded_ThrowsManagerException() {
        stubDAO.setExistingGrade(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE));

        assertTrue(e.getMessage().contains(PERIOD));
    }



    @Test
    void updateFinalGrade_ValidGrade_ExecutesWithoutException() throws ManagerException {
        stubDAO.setExistingGrade(true);

        gradingManager.updateFinalGrade(PRACTITIONER_ID, PERIOD, 9.5);

        assertEquals(9.5, stubDAO.getLastUpdatedFinalGrade());
    }

    @Test
    void updateFinalGrade_GradeZero_ExecutesWithoutException() throws ManagerException {
        stubDAO.setExistingGrade(true);

        gradingManager.updateFinalGrade(PRACTITIONER_ID, PERIOD, 0.0);

        assertEquals(0.0, stubDAO.getLastUpdatedFinalGrade());
    }



    @Test
    void updateFinalGrade_GradeAbove10_ThrowsManagerException() {
        stubDAO.setExistingGrade(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> gradingManager.updateFinalGrade(PRACTITIONER_ID, PERIOD, 10.5));

        assertNotNull(e.getMessage());
    }

    @Test
    void updateFinalGrade_GradeNotFound_ThrowsManagerException() {
        stubDAO.setExistingGrade(false);

        ManagerException e = assertThrows(ManagerException.class,
                () -> gradingManager.updateFinalGrade(PRACTITIONER_ID, PERIOD, 8.0));

        assertNotNull(e.getMessage());
    }


    @Test
    void previewTentativeGrade_WithEvaluatedReports_ReturnsTentativeAverage() throws ManagerException {
        stubDAO.setTentativeGrade(7.5);

        double result = gradingManager.previewTentativeGrade(PRACTITIONER_ID);

        assertEquals(7.5, result);
    }

    @Test
    void previewTentativeGrade_WithNoReports_ReturnsZero() throws ManagerException {
        stubDAO.setTentativeGrade(0.0);

        double result = gradingManager.previewTentativeGrade(PRACTITIONER_ID);

        assertEquals(0.0, result);
    }

    // ── getGradesByProfessor ──────────────────────────────────────────────

    @Test
    void getGradesByProfessor_WithGrades_ReturnsList() throws ManagerException {
        stubDAO.addGrade(new PractitionerGrade());

        List<PractitionerGrade> result = gradingManager.getGradesByProfessor(PROFESSOR_ID);

        assertFalse(result.isEmpty());
    }

    @Test
    void getGradesByProfessor_WithNoGrades_ReturnsEmptyList() throws ManagerException {
        List<PractitionerGrade> result = gradingManager.getGradesByProfessor(PROFESSOR_ID);

        assertTrue(result.isEmpty());
    }

    private static class StubPractitionerGradeDAO implements IPractitionerGradeDAO {

        private double tentativeGrade      = 0.0;
        private boolean existingGrade      = false;
        private double lastUpdatedFinalGrade = -1.0;
        private final List<PractitionerGrade> grades = new ArrayList<>();
        private int nextId = 1;

        void setTentativeGrade(double grade) {
            this.tentativeGrade = grade;
        }

        void setExistingGrade(boolean exists) {
            this.existingGrade = exists;
        }

        void addGrade(PractitionerGrade grade) {
            this.grades.add(grade);
        }

        double getLastUpdatedFinalGrade() {
            return lastUpdatedFinalGrade;
        }

        @Override
        public int insertPractitionerGrade(PractitionerGrade grade) throws DAOException {
            return nextId++;
        }

        @Override
        public boolean updateFinalGrade(int gradeId, double finalGrade) throws DAOException {
            lastUpdatedFinalGrade = finalGrade;
            return true;
        }

        @Override
        public PractitionerGrade getGradeByPractitionerAndPeriod(int practitionerId, String period) throws DAOException {
            PractitionerGrade found = null;

            if (existingGrade) {
                found = new PractitionerGrade();
                found.setGradeId(1);
                found.setPractitionerId(practitionerId);
                found.setPeriod(period);
                found.setTentativeGrade(tentativeGrade);
                found.setFinalGrade(8.0);
            }

            return found;
        }

        @Override
        public List<PractitionerGrade> getGradesByProfessor(int professorId) throws DAOException {
            return new ArrayList<>(grades);
        }

        @Override
        public double calculateTentativeGrade(int practitionerId) throws DAOException {
            return tentativeGrade;
        }
    }
}
