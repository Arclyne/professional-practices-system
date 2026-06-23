package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IMonthlyReportDAO;
import mx.uv.fei.domain.dto.MonthlyReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class MonthlyReportDAOTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int STORED_REPORT_ID = 1;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IMonthlyReportDAO monthlyReportDAO;

    private MonthlyReport juneReport;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        juneReport = new MonthlyReport();
        juneReport.setPractitionerId(PRACTITIONER_ID);
        juneReport.setMonthName("Junio");
        juneReport.setYear(2026);
        juneReport.setStartDate(Date.valueOf("2026-06-01"));
        juneReport.setEndDate(Date.valueOf("2026-06-30"));
        juneReport.setStatus("Pendiente de Firma");
    }

    private MonthlyReport buildStoredMayReport() {
        MonthlyReport storedReport = new MonthlyReport();
        storedReport.setReportId(STORED_REPORT_ID);
        storedReport.setPractitionerId(PRACTITIONER_ID);
        storedReport.setMonthName("Mayo");
        storedReport.setYear(2026);
        storedReport.setStartDate(Date.valueOf("2026-05-01"));
        storedReport.setEndDate(Date.valueOf("2026-05-31"));
        storedReport.setStatus("Borrador");
        return storedReport;
    }

    @Test
    void insertReport_ValidReport_ReturnsGeneratedId() throws DAOException {
        int generatedId = monthlyReportDAO.insertReport(juneReport);

        assertTrue(generatedId > 0);
    }

    @Test
    void getReportsByPractitioner_WithExistingReports_ReturnsExpectedList() throws DAOException {
        List<MonthlyReport> expectedReports = new ArrayList<>();
        expectedReports.add(buildStoredMayReport());

        List<MonthlyReport> resultReports = monthlyReportDAO.getReportsByPractitioner(PRACTITIONER_ID);

        assertEquals(expectedReports, resultReports);
    }

    @Test
    void getSubmittedReports_WithSubmittedReports_ReturnsExpectedList() throws DAOException {
        juneReport.setStatus("Entregado");
        int generatedId = monthlyReportDAO.insertReport(juneReport);

        List<MonthlyReport> expectedReports = new ArrayList<>();
        MonthlyReport submittedReport = new MonthlyReport();
        submittedReport.setReportId(generatedId);
        submittedReport.setPractitionerId(PRACTITIONER_ID);
        submittedReport.setMonthName("Junio");
        submittedReport.setYear(2026);
        submittedReport.setStartDate(Date.valueOf("2026-06-01"));
        submittedReport.setEndDate(Date.valueOf("2026-06-30"));
        submittedReport.setStatus("Entregado");
        expectedReports.add(submittedReport);

        List<MonthlyReport> resultReports = monthlyReportDAO.getSubmittedReports();

        assertEquals(expectedReports, resultReports);
    }

    @Test
    void getReportById_ExistingId_ReturnsReport() throws DAOException {
        MonthlyReport expectedReport = buildStoredMayReport();

        MonthlyReport recoveredReport = monthlyReportDAO.getReportById(STORED_REPORT_ID);

        assertEquals(expectedReport, recoveredReport);
    }

    @Test
    void updateReport_ValidModifiedData_ReturnsTrue() throws DAOException {
        juneReport.setMonthName("Julio");
        juneReport.setStatus("Entregado");

        assertDoesNotThrow(() -> monthlyReportDAO.updateReport(juneReport, STORED_REPORT_ID));
    }

    @Test
    void insertReport_NonExistentPractitioner_ThrowsDAOException() {
        juneReport.setPractitionerId(NON_EXISTENT_ID);

        assertThrows(DAOException.class, () -> monthlyReportDAO.insertReport(juneReport));
    }

    @Test
    void updateReport_NonExistentId_ReturnsFalse() throws DAOException {
        assertDoesNotThrow(() -> monthlyReportDAO.updateReport(juneReport, NON_EXISTENT_ID));
    }
}
