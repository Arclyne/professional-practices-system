package mx.uv.fei.domain.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.dataaccess.interfaces.IProgressReportDAO;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.ProjectPostulation;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.exceptions.ManagerException;

import static org.junit.jupiter.api.Assertions.*;

public class ProgressReportManagerTest {

    private static final int    PRACTITIONER_ID = 123;
    private static final Date   PERIOD_START    = Date.valueOf("2026-06-01");
    private static final Date   PERIOD_END      = Date.valueOf("2026-11-30");
    private static final double HOURS_210       = 215.0;
    private static final double HOURS_420       = 425.0;
    private static final double HOURS_BELOW_210 = 100.0;
    private static final double HOURS_BELOW_420 = 300.0;

    private ProgressReportManager progressReportManager;
    private StubProgressReportDAO stubProgressReportDAO;
    private StubPostulationDAO stubPostulationDAO;

    @BeforeEach
    void setUp() {
        stubProgressReportDAO = new StubProgressReportDAO();
        stubPostulationDAO = new StubPostulationDAO();

        progressReportManager = new ProgressReportManager(stubProgressReportDAO, stubPostulationDAO);

        stubPostulationDAO.setHasAssignedProject(true);
    }



    @Test
    void generateProgressReport_NoAssignedProject_ThrowsManagerException() {
        stubPostulationDAO.setHasAssignedProject(false);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.generateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END));

        assertTrue(e.getMessage().contains("sin tener un proyecto asignado"));
    }

    @Test
    void generateProgressReport_ProjectCheckThrowsDAOException_ThrowsManagerException() {

        stubPostulationDAO.setThrowDaoException(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.generateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END));

        assertTrue(e.getMessage().contains("verificar el estado de tu proyecto"));
    }


    @Test
    void generateProgressReport_IntermediateWith215Hours_ReturnsReport() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(HOURS_210);

        ProgressReport result = progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END);

        assertNotNull(result);
    }

    @Test
    void generateProgressReport_IntermediateWith215Hours_SetsCorrectType() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(HOURS_210);

        ProgressReport result = progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END);

        assertEquals("Intermedio", result.getReportType());
    }

    @Test
    void generateProgressReport_FinalWith425Hours_ReturnsReport() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(HOURS_420);
        stubProgressReportDAO.setExistingIntermediate(true);

        ProgressReport result = progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.FINAL, PERIOD_START, PERIOD_END);

        assertNotNull(result);
    }

    @Test
    void generateProgressReport_IntermediateWith215Hours_StoresTotalHours() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(HOURS_210);

        ProgressReport result = progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END);

        assertEquals(HOURS_210, result.getTotalHoursAtSubmission());
    }

    @Test
    void generateProgressReport_IntermediateWith100Hours_ThrowsManagerException() {
        stubProgressReportDAO.setAccumulatedHours(HOURS_BELOW_210);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.generateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END));

        assertTrue(e.getMessage().contains("210 horas"));
    }

    @Test
    void generateProgressReport_FinalWith300Hours_ThrowsManagerException() {
        stubProgressReportDAO.setAccumulatedHours(HOURS_BELOW_420);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.generateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.FINAL, PERIOD_START, PERIOD_END));

        assertTrue(e.getMessage().contains("420 horas"));
    }

    @Test
    void generateProgressReport_IntermediateAlreadyExists_ThrowsManagerException() {
        stubProgressReportDAO.setAccumulatedHours(HOURS_210);
        stubProgressReportDAO.setExistingIntermediate(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.generateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END));

        assertTrue(e.getMessage().contains("Intermedio"));
    }

    @Test
    void submitSignedProgressReport_ValidFile_UpdatesStatus() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(HOURS_210);
        stubProgressReportDAO.setExistingIntermediate(true);

        progressReportManager.submitSignedProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, "/files/reporte_firmado.pdf");

        assertEquals("Entregado", stubProgressReportDAO.getLastUpdatedReport().getStatus());
    }

    @Test
    void submitSignedProgressReport_ValidFile_SetsFileUrl() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(HOURS_210);
        stubProgressReportDAO.setExistingIntermediate(true);

        progressReportManager.submitSignedProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, "/files/firmado.pdf");

        assertEquals("/files/firmado.pdf", stubProgressReportDAO.getLastUpdatedReport().getSignedFileUrl());
    }

    @Test
    void submitSignedProgressReport_NullFile_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.submitSignedProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, null));

        assertNotNull(e.getMessage());
    }

    @Test
    void submitSignedProgressReport_BlankFile_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.submitSignedProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, "   "));

        assertNotNull(e.getMessage());
    }

    @Test
    void submitSignedProgressReport_ReportNotFound_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(false);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.submitSignedProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, "/files/ok.pdf"));

        assertTrue(e.getMessage().contains("Intermedio"));
    }

    @Test
    void evaluateProgressReport_ValidGradeAndFeedback_UpdatesStatus() throws ManagerException {
        stubProgressReportDAO.setExistingIntermediate(true);

        progressReportManager.evaluateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, 9.0, "Excelente trabajo.");

        assertEquals("Evaluado", stubProgressReportDAO.getLastUpdatedReport().getStatus());
    }

    @Test
    void evaluateProgressReport_ValidGrade_SetsGradeOnReport() throws ManagerException {
        stubProgressReportDAO.setExistingIntermediate(true);

        progressReportManager.evaluateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, 8.5, "Buen avance.");

        assertEquals(8.5, stubProgressReportDAO.getLastUpdatedReport().getGrade());
    }

    @Test
    void evaluateProgressReport_GradeAbove10_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.evaluateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, 11.0, "Retroalimentación."));

        assertNotNull(e.getMessage());
    }

    @Test
    void evaluateProgressReport_NegativeGrade_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.evaluateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, -1.0, "Retroalimentación."));

        assertNotNull(e.getMessage());
    }

    @Test
    void evaluateProgressReport_NullFeedback_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.evaluateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, 7.0, null));

        assertNotNull(e.getMessage());
    }

    @Test
    void evaluateProgressReport_BlankFeedback_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        ManagerException e = assertThrows(ManagerException.class,
                () -> progressReportManager.evaluateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, 7.0, "   "));

        assertNotNull(e.getMessage());
    }

    @Test
    void getSubmittedProgressReports_WithReports_ReturnsList() throws ManagerException {
        stubProgressReportDAO.addSubmittedReport(new ProgressReport());

        List<ProgressReport> result = progressReportManager.getSubmittedProgressReports();

        assertFalse(result.isEmpty());
    }

    @Test
    void getProgressReportsByPractitioner_NoReports_ReturnsEmptyList() throws ManagerException {
        List<ProgressReport> result = progressReportManager.getProgressReportsByPractitioner(PRACTITIONER_ID);

        assertTrue(result.isEmpty());
    }

    // --- CLASES STUB ---

    private static class StubProgressReportDAO implements IProgressReportDAO {

        private double accumulatedHours = 0.0;
        private boolean existingIntermediate = false;
        private ProgressReport lastUpdatedReport;
        private final List<ProgressReport> submittedReports = new ArrayList<>();
        private int nextId = 1;

        void setAccumulatedHours(double hours) {
            this.accumulatedHours = hours;
        }

        void setExistingIntermediate(boolean exists) {
            this.existingIntermediate = exists;
        }

        void addSubmittedReport(ProgressReport report) {
            this.submittedReports.add(report);
        }

        ProgressReport getLastUpdatedReport() {
            return lastUpdatedReport;
        }

        @Override
        public int insertProgressReport(ProgressReport report) throws DAOException {
            return nextId++;
        }

        @Override
        public boolean updateProgressReport(ProgressReport report, int reportId) throws DAOException {
            lastUpdatedReport = report;
            return true;
        }

        @Override
        public ProgressReport getProgressReportByPractitionerAndType(int practitionerId, String reportType) throws DAOException {
            ProgressReport found = null;

            if ("Intermedio".equals(reportType) && existingIntermediate) {
                found = new ProgressReport();
                found.setReportId(1);
                found.setPractitionerId(practitionerId);
                found.setReportType("Intermedio");
                found.setStatus("Pendiente de Firma");
            }

            return found;
        }

        @Override
        public List<ProgressReport> getProgressReportsByPractitioner(int practitionerId) throws DAOException {
            return new ArrayList<>();
        }

        @Override
        public List<ProgressReport> getSubmittedProgressReports() throws DAOException {
            return new ArrayList<>(submittedReports);
        }

        @Override
        public double getTotalAccumulatedHours(int practitionerId) throws DAOException {
            return accumulatedHours;
        }
    }

    private static class StubPostulationDAO implements IPostulationDAO {
        private boolean hasAssignedProject = true;
        private boolean throwDaoException = false;

        void setHasAssignedProject(boolean hasAssignedProject) {
            this.hasAssignedProject = hasAssignedProject;
        }

        void setThrowDaoException(boolean throwDaoException) {
            this.throwDaoException = throwDaoException;
        }

        @Override
        public boolean hasAssignedProject(int practitionerId) throws DAOException {
            if (throwDaoException) {
                throw new DAOException("Error de BD simulado",new java.sql.SQLException("Simulated"));
            }
            return hasAssignedProject;
        }

        // Métodos vacíos (no usados en estas pruebas pero requeridos por la interfaz)
        @Override public boolean hasPractitionerSubmittedPriorities(int id) { return false; }
        @Override public boolean insertProjectPriorities(int id, List<Project> list) { return false; }
        @Override public List<ProjectPostulation> retrievePractitionerPostulations(int id) { return new ArrayList<>(); }
        @Override public boolean assignProjectUsingStoredProcedure(int pId, int prjId) { return false; }
    }
}