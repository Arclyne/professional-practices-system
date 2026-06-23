package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerGradeDAO;
import mx.uv.fei.domain.dto.PractitionerGrade;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GradingManagerTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int PROFESSOR_ID = 68;
    private static final String PERIOD = "Junio-Diciembre 2026";
    private static final double VALID_GRADE = 8.5;
    private static final double TENTATIVE_GRADE = 7.8;
    private static final double MINIMUM_GRADE = 0.0;
    private static final double MAXIMUM_GRADE = 10.0;
    private static final double GRADE_ABOVE_MAXIMUM = 10.1;
    private static final double NEGATIVE_GRADE = -0.1;
    private static final double UPDATED_FINAL_GRADE = 9.5;

    private GradingManager gradingManager;
    private StubPractitionerGradeDAO stubGradeDAO;

    @BeforeEach
    void setUp() {
        stubGradeDAO = new StubPractitionerGradeDAO();
        gradingManager = new GradingManager(stubGradeDAO);
    }

    @Test
    void registerGrade_ValidData_ReturnsGradeRecord() throws ManagerException {
        stubGradeDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade registeredGrade = gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE);

        assertNotNull(registeredGrade);
    }

    @Test
    void registerGrade_ValidData_SetsPractitionerId() throws ManagerException {
        stubGradeDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade registeredGrade = gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE);

        assertEquals(PRACTITIONER_ID, registeredGrade.getPractitionerId());
    }

    @Test
    void registerGrade_ValidData_SetsTentativeGrade() throws ManagerException {
        stubGradeDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade registeredGrade = gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE);

        assertEquals(TENTATIVE_GRADE, registeredGrade.getTentativeGrade());
    }

    @Test
    void registerGrade_ValidData_SetsFinalGrade() throws ManagerException {
        stubGradeDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade registeredGrade = gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE);

        assertEquals(VALID_GRADE, registeredGrade.getFinalGrade());
    }

    @Test
    void registerGrade_GradeZero_IsValidMinimum() throws ManagerException {
        stubGradeDAO.setTentativeGrade(MINIMUM_GRADE);

        PractitionerGrade registeredGrade = gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, MINIMUM_GRADE);

        assertNotNull(registeredGrade);
    }

    @Test
    void registerGrade_GradeTen_IsValidMaximum() throws ManagerException {
        stubGradeDAO.setTentativeGrade(TENTATIVE_GRADE);

        PractitionerGrade registeredGrade = gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, MAXIMUM_GRADE);

        assertNotNull(registeredGrade);
    }

    @Test
    void registerGrade_GradeAboveMaximum_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, GRADE_ABOVE_MAXIMUM));
    }

    @Test
    void registerGrade_NegativeGrade_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, NEGATIVE_GRADE));
    }

    @Test
    void registerGrade_NullPeriod_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, null, VALID_GRADE));
    }

    @Test
    void registerGrade_BlankPeriod_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, "   ", VALID_GRADE));
    }

    @Test
    void registerGrade_AlreadyGraded_ThrowsManagerException() {
        stubGradeDAO.setExistingGrade(true);

        assertThrows(ManagerException.class,
                () -> gradingManager.registerGrade(PRACTITIONER_ID, PROFESSOR_ID, PERIOD, VALID_GRADE));
    }

    @Test
    void updateFinalGrade_ValidGrade_ExecutesWithoutException() throws ManagerException {
        stubGradeDAO.setExistingGrade(true);

        gradingManager.updateFinalGrade(PRACTITIONER_ID, PERIOD, UPDATED_FINAL_GRADE);

        assertEquals(UPDATED_FINAL_GRADE, stubGradeDAO.getLastUpdatedFinalGrade());
    }

    @Test
    void updateFinalGrade_GradeZero_ExecutesWithoutException() throws ManagerException {
        stubGradeDAO.setExistingGrade(true);

        gradingManager.updateFinalGrade(PRACTITIONER_ID, PERIOD, MINIMUM_GRADE);

        assertEquals(MINIMUM_GRADE, stubGradeDAO.getLastUpdatedFinalGrade());
    }

    @Test
    void updateFinalGrade_GradeAboveMaximum_ThrowsManagerException() {
        stubGradeDAO.setExistingGrade(true);

        assertThrows(ManagerException.class,
                () -> gradingManager.updateFinalGrade(PRACTITIONER_ID, PERIOD, GRADE_ABOVE_MAXIMUM));
    }

    @Test
    void updateFinalGrade_GradeNotFound_ThrowsManagerException() {
        stubGradeDAO.setExistingGrade(false);

        assertThrows(ManagerException.class,
                () -> gradingManager.updateFinalGrade(PRACTITIONER_ID, PERIOD, VALID_GRADE));
    }

    @Test
    void previewTentativeGrade_WithEvaluatedReports_ReturnsTentativeAverage() throws ManagerException {
        stubGradeDAO.setTentativeGrade(TENTATIVE_GRADE);

        double tentativeGrade = gradingManager.previewTentativeGrade(PRACTITIONER_ID);

        assertEquals(TENTATIVE_GRADE, tentativeGrade);
    }

    @Test
    void previewTentativeGrade_WithNoReports_ReturnsZero() throws ManagerException {
        stubGradeDAO.setTentativeGrade(MINIMUM_GRADE);

        double tentativeGrade = gradingManager.previewTentativeGrade(PRACTITIONER_ID);

        assertEquals(MINIMUM_GRADE, tentativeGrade);
    }

    @Test
    void getGradesByProfessor_WithGrades_ReturnsList() throws ManagerException {
        PractitionerGrade storedGrade = new PractitionerGrade();
        storedGrade.setGradeId(1);
        storedGrade.setPractitionerId(PRACTITIONER_ID);
        stubGradeDAO.addGrade(storedGrade);
        List<PractitionerGrade> expectedGrades = new ArrayList<>();
        expectedGrades.add(storedGrade);

        List<PractitionerGrade> resultGrades = gradingManager.getGradesByProfessor(PROFESSOR_ID);

        assertEquals(expectedGrades, resultGrades);
    }

    @Test
    void getGradesByProfessor_WithNoGrades_ReturnsEmptyList() throws ManagerException {
        List<PractitionerGrade> expectedGrades = new ArrayList<>();

        List<PractitionerGrade> resultGrades = gradingManager.getGradesByProfessor(PROFESSOR_ID);

        assertEquals(expectedGrades, resultGrades);
    }

    private static class StubPractitionerGradeDAO implements IPractitionerGradeDAO {

        private static final double DEFAULT_FINAL_GRADE = 8.0;

        private double tentativeGrade = 0.0;
        private boolean existingGrade = false;
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
        public void updateFinalGrade(int gradeId, double finalGrade) throws DAOException {
            lastUpdatedFinalGrade = finalGrade;
        }

        @Override
        public PractitionerGrade getGradeByPractitionerAndPeriod(int practitionerId, String period) throws DAOException {
            PractitionerGrade foundGrade = null;

            if (existingGrade) {
                foundGrade = new PractitionerGrade();
                foundGrade.setGradeId(1);
                foundGrade.setPractitionerId(practitionerId);
                foundGrade.setPeriod(period);
                foundGrade.setTentativeGrade(tentativeGrade);
                foundGrade.setFinalGrade(DEFAULT_FINAL_GRADE);
            }

            return foundGrade;
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
