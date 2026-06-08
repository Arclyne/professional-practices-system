package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        testReport.setMonthName("Junio");
        testReport.setYear(2026);
        testReport.setStartDate(Date.valueOf("2026-06-01"));
        testReport.setEndDate(Date.valueOf("2026-06-30"));
        testReport.setStatus("Pendiente de Firma");
    }

    @Test
    void insertReport_ValidReport_ReturnsGeneratedId() throws DAOException {
        int generatedId = monthlyReportDAO.insertReport(testReport);
        assertTrue(generatedId > 0);
    }

    @Test
    void getReportsByPractitioner_WithExistingReports_ReturnsList() throws DAOException {
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
        MonthlyReport expectedReport = new MonthlyReport();
        expectedReport.setReportId(1);
        expectedReport.setPractitionerId(123);
        expectedReport.setMonthName("Mayo");
        expectedReport.setYear(2026);
        expectedReport.setStartDate(Date.valueOf("2026-05-01"));
        expectedReport.setEndDate(Date.valueOf("2026-05-31"));
        expectedReport.setStatus("Borrador");

        MonthlyReport recovered = monthlyReportDAO.getReportById(1);
        assertEquals(expectedReport, recovered);
    }

    @Test
    void updateReport_ValidModifiedData_ReturnsTrue() throws DAOException {
        testReport.setMonthName("Julio");
        testReport.setStatus("Entregado");

        boolean isUpdated = monthlyReportDAO.updateReport(testReport, 1);
        assertTrue(isUpdated);
    }

    @Test
    void insertReport_NonExistentPractitioner_ThrowsDAOException() {
        testReport.setPractitionerId(9999);
        assertThrows(DAOException.class, () -> {
            monthlyReportDAO.insertReport(testReport);
        });
    }

    @Test
    void updateReport_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = monthlyReportDAO.updateReport(testReport, 9999);
        assertFalse(isUpdated);
    }
}