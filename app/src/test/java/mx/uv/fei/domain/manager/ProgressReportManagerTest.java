package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Date;
import java.sql.SQLException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProgressReportManagerTest {

    private static final int PRACTITIONER_ID = 123;
    private static final Date PERIOD_START = Date.valueOf("2026-06-01");
    private static final Date PERIOD_END = Date.valueOf("2026-11-30");
    private static final double VALID_INTERMEDIATE_HOURS = 215.0;
    private static final double VALID_FINAL_HOURS = 425.0;
    private static final double INSUFFICIENT_INTERMEDIATE_HOURS = 100.0;
    private static final double INSUFFICIENT_FINAL_HOURS = 300.0;
    private static final double VALID_GRADE = 9.0;
    private static final double GRADE_ABOVE_MAXIMUM = 11.0;
    private static final double NEGATIVE_GRADE = -1.0;

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

        assertThrows(ManagerException.class, () -> progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END));
    }

    @Test
    void generateProgressReport_ProjectCheckThrowsDAOException_ThrowsManagerException() {
        stubPostulationDAO.setThrowDaoException(true);

        assertThrows(ManagerException.class, () -> progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END));
    }

    @Test
    void generateProgressReport_StartDateAfterEndDate_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_END, PERIOD_START));
    }

    @Test
    void generateProgressReport_ProjectCheckThrowsDAOException_PreservesCause() {
        stubPostulationDAO.setThrowDaoException(true);

        ManagerException thrownException = assertThrows(ManagerException.class,
                () -> progressReportManager.generateProgressReport(
                        PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END));

        assertInstanceOf(DAOException.class, thrownException.getCause());
    }

    @Test
    void generateProgressReport_IntermediateWithEnoughHours_ReturnsReport() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(VALID_INTERMEDIATE_HOURS);

        ProgressReport generatedReport = progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END);

        assertNotNull(generatedReport);
    }

    @Test
    void generateProgressReport_IntermediateWithEnoughHours_SetsCorrectType() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(VALID_INTERMEDIATE_HOURS);

        ProgressReport generatedReport = progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END);

        assertEquals("Intermedio", generatedReport.getReportType());
    }

    @Test
    void generateProgressReport_FinalWithEnoughHours_ReturnsReport() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(VALID_FINAL_HOURS);
        stubProgressReportDAO.setExistingIntermediate(true);
        stubProgressReportDAO.setIntermediateStatus("Entregado");

        ProgressReport generatedReport = progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.FINAL, PERIOD_START, PERIOD_END);

        assertNotNull(generatedReport);
    }

    @Test
    void generateProgressReport_FinalWithoutIntermediate_ThrowsManagerException() {
        stubProgressReportDAO.setAccumulatedHours(VALID_FINAL_HOURS);
        stubProgressReportDAO.setExistingIntermediate(false);

        assertThrows(ManagerException.class, () -> progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.FINAL, PERIOD_START, PERIOD_END));
    }

    @Test
    void generateProgressReport_FinalWithPendingIntermediate_ThrowsManagerException() {
        stubProgressReportDAO.setAccumulatedHours(VALID_FINAL_HOURS);
        stubProgressReportDAO.setExistingIntermediate(true);
        stubProgressReportDAO.setIntermediateStatus("Pendiente de Firma");

        assertThrows(ManagerException.class, () -> progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.FINAL, PERIOD_START, PERIOD_END));
    }

    @Test
    void generateProgressReport_IntermediateWithEnoughHours_StoresTotalHours() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(VALID_INTERMEDIATE_HOURS);

        ProgressReport generatedReport = progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END);

        assertEquals(VALID_INTERMEDIATE_HOURS, generatedReport.getTotalHoursAtSubmission());
    }

    @Test
    void generateProgressReport_IntermediateWithInsufficientHours_ThrowsManagerException() {
        stubProgressReportDAO.setAccumulatedHours(INSUFFICIENT_INTERMEDIATE_HOURS);

        assertThrows(ManagerException.class, () -> progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END));
    }

    @Test
    void generateProgressReport_FinalWithInsufficientHours_ThrowsManagerException() {
        stubProgressReportDAO.setAccumulatedHours(INSUFFICIENT_FINAL_HOURS);

        assertThrows(ManagerException.class, () -> progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.FINAL, PERIOD_START, PERIOD_END));
    }

    @Test
    void generateProgressReport_IntermediateAlreadyExists_ThrowsManagerException() {
        stubProgressReportDAO.setAccumulatedHours(VALID_INTERMEDIATE_HOURS);
        stubProgressReportDAO.setExistingIntermediate(true);

        assertThrows(ManagerException.class, () -> progressReportManager.generateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, PERIOD_START, PERIOD_END));
    }

    @Test
    void submitSignedProgressReport_ValidFile_UpdatesStatus() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(VALID_INTERMEDIATE_HOURS);
        stubProgressReportDAO.setExistingIntermediate(true);

        progressReportManager.submitSignedProgressReport(PRACTITIONER_ID, ProgressReportType.INTERMEDIO,
                "/documentos/reporte_intermedio_firmado.pdf");

        assertEquals("Entregado", stubProgressReportDAO.getLastUpdatedReport().getStatus());
    }

    @Test
    void submitSignedProgressReport_ValidFile_SetsFileUrl() throws ManagerException {
        stubProgressReportDAO.setAccumulatedHours(VALID_INTERMEDIATE_HOURS);
        stubProgressReportDAO.setExistingIntermediate(true);

        progressReportManager.submitSignedProgressReport(PRACTITIONER_ID, ProgressReportType.INTERMEDIO,
                "/documentos/reporte_firmado.pdf");

        assertEquals("/documentos/reporte_firmado.pdf", stubProgressReportDAO.getLastUpdatedReport().getSignedFileUrl());
    }

    @Test
    void submitSignedProgressReport_NullFile_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        assertThrows(ManagerException.class, () -> progressReportManager.submitSignedProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, null));
    }

    @Test
    void submitSignedProgressReport_BlankFile_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        assertThrows(ManagerException.class, () -> progressReportManager.submitSignedProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, "   "));
    }

    @Test
    void submitSignedProgressReport_ReportNotFound_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(false);

        assertThrows(ManagerException.class, () -> progressReportManager.submitSignedProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, "/documentos/reporte_firmado.pdf"));
    }

    @Test
    void evaluateProgressReport_ValidGradeAndFeedback_UpdatesStatus() throws ManagerException {
        stubProgressReportDAO.setExistingIntermediate(true);

        progressReportManager.evaluateProgressReport(PRACTITIONER_ID, ProgressReportType.INTERMEDIO,
                VALID_GRADE, "Excelente trabajo.");

        assertEquals("Evaluado", stubProgressReportDAO.getLastUpdatedReport().getStatus());
    }

    @Test
    void evaluateProgressReport_ValidGrade_SetsGradeOnReport() throws ManagerException {
        stubProgressReportDAO.setExistingIntermediate(true);

        progressReportManager.evaluateProgressReport(PRACTITIONER_ID, ProgressReportType.INTERMEDIO,
                VALID_GRADE, "Buen avance.");

        assertEquals(VALID_GRADE, stubProgressReportDAO.getLastUpdatedReport().getGrade());
    }

    @Test
    void evaluateProgressReport_GradeAboveMaximum_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        assertThrows(ManagerException.class, () -> progressReportManager.evaluateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, GRADE_ABOVE_MAXIMUM, "Retroalimentacion."));
    }

    @Test
    void evaluateProgressReport_NegativeGrade_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        assertThrows(ManagerException.class, () -> progressReportManager.evaluateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, NEGATIVE_GRADE, "Retroalimentacion."));
    }

    @Test
    void evaluateProgressReport_NullFeedback_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        assertThrows(ManagerException.class, () -> progressReportManager.evaluateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, VALID_GRADE, null));
    }

    @Test
    void evaluateProgressReport_BlankFeedback_ThrowsManagerException() {
        stubProgressReportDAO.setExistingIntermediate(true);

        assertThrows(ManagerException.class, () -> progressReportManager.evaluateProgressReport(
                PRACTITIONER_ID, ProgressReportType.INTERMEDIO, VALID_GRADE, "   "));
    }

    @Test
    void getSubmittedProgressReports_WithReports_ReturnsExpectedList() throws ManagerException {
        ProgressReport submittedReport = new ProgressReport();
        stubProgressReportDAO.addSubmittedReport(submittedReport);
        List<ProgressReport> expectedReports = new ArrayList<>();
        expectedReports.add(submittedReport);

        List<ProgressReport> resultReports = progressReportManager.getSubmittedProgressReports();

        assertEquals(expectedReports, resultReports);
    }

    @Test
    void getProgressReportsByPractitioner_NoReports_ReturnsEmptyList() throws ManagerException {
        List<ProgressReport> expectedReports = new ArrayList<>();

        List<ProgressReport> resultReports = progressReportManager.getProgressReportsByPractitioner(PRACTITIONER_ID);

        assertEquals(expectedReports, resultReports);
    }

    private static class StubProgressReportDAO implements IProgressReportDAO {

        private double accumulatedHours = 0.0;
        private boolean existingIntermediate = false;
        private String intermediateStatus = "Pendiente de Firma";
        private ProgressReport lastUpdatedReport;
        private final List<ProgressReport> submittedReports = new ArrayList<>();
        private int nextId = 1;

        void setAccumulatedHours(double hours) {
            this.accumulatedHours = hours;
        }

        void setExistingIntermediate(boolean exists) {
            this.existingIntermediate = exists;
        }

        void setIntermediateStatus(String intermediateStatus) {
            this.intermediateStatus = intermediateStatus;
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
        public void updateProgressReport(ProgressReport report, int reportId) throws DAOException {
            lastUpdatedReport = report;
        }

        @Override
        public ProgressReport getProgressReportByPractitionerAndType(int practitionerId, String reportType) throws DAOException {
            ProgressReport foundReport = null;

            if ("Intermedio".equals(reportType) && existingIntermediate) {
                foundReport = new ProgressReport();
                foundReport.setReportId(1);
                foundReport.setPractitionerId(practitionerId);
                foundReport.setReportType("Intermedio");
                foundReport.setStatus(intermediateStatus);
            }

            return foundReport;
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
                throw new DAOException("Error de BD simulado", new SQLException("Fallo simulado de conexion"));
            }
            return hasAssignedProject;
        }

        @Override
        public boolean hasPractitionerSubmittedPriorities(int practitionerId) {
            return false;
        }

        @Override
        public void insertProjectPriorities(int practitionerId, List<Project> projects) {
        }

        @Override
        public List<ProjectPostulation> retrievePractitionerPostulations(int practitionerId) {
            return new ArrayList<>();
        }

        @Override
        public void assignProjectUsingStoredProcedure(int practitionerId, int projectId) {
        }
    }
}
