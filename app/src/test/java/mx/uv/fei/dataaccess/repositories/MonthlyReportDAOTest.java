package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.sql.SQLException;
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

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IMonthlyReportDAO monthlyReportDAO;

    private MonthlyReport testReport;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testReport = new MonthlyReport();
        testReport.setPractitionerId(PRACTITIONER_ID);
        testReport.setMonthName("Mayo");
        testReport.setYear(2026);
        testReport.setStartDate(Date.valueOf("2026-05-01"));
        testReport.setEndDate(Date.valueOf("2026-05-31"));
        testReport.setStatus("Pendiente de Firma");
        testReport.setSignedFileUrl(null);
    }

    @Test
    void insertReport_ValidReport_ReturnsGeneratedId() throws DAOException {

        int generatedId = monthlyReportDAO.insertReport(testReport);

        assertTrue(generatedId > 0);
    }

    @Test
    void getReportsByPractitioner_WithExistingReports_ReturnsList() throws DAOException {
        monthlyReportDAO.insertReport(testReport);

        List<MonthlyReport> resultList = monthlyReportDAO.getReportsByPractitioner(PRACTITIONER_ID);

        assertFalse(resultList.isEmpty());
    }

    @Test
    void getSubmittedReports_WithSubmittedReports_ReturnsList() throws DAOException {
        testReport.setStatus("Entregado");
        monthlyReportDAO.insertReport(testReport);

        List<MonthlyReport> resultList = monthlyReportDAO.getSubmittedReports();

        assertFalse(resultList.isEmpty());
    }

    @Test
    void getReportById_ExistingId_ReturnsReport() throws DAOException {
        int generatedId = monthlyReportDAO.insertReport(testReport);

        MonthlyReport recovered = monthlyReportDAO.getReportById(generatedId);

        assertTrue(recovered.getReportId() > 0);
    }

    @Test
    void updateReport_ValidModifiedData_ReturnsTrue() throws DAOException {
        int generatedId = monthlyReportDAO.insertReport(testReport);
        testReport.setMonthName("Junio");
        testReport.setYear(2026);
        testReport.setStartDate(Date.valueOf("2026-06-01"));
        testReport.setEndDate(Date.valueOf("2026-06-30"));
        testReport.setStatus("Entregado");
        testReport.setGrade(9.0);
        testReport.setProfessorFeedback("Buen trabajo.");

        boolean isUpdated = monthlyReportDAO.updateReport(testReport, generatedId);

        assertTrue(isUpdated);
    }
}