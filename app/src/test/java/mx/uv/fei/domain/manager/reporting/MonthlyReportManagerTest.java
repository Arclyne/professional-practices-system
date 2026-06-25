package mx.uv.fei.domain.manager.reporting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class MonthlyReportManagerTest {

    private static final int STORED_PRACTITIONER_ID = 123;
    private static final int STORED_REPORT_ID = 1;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private MonthlyReportManager reportManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void createReportAndLinkActivities_ValidData_DoesNotThrow() {
        MonthlyReport juneReport = new MonthlyReport();
        juneReport.setPractitionerId(STORED_PRACTITIONER_ID);
        juneReport.setMonthName("Junio");
        juneReport.setYear(2026);
        juneReport.setStartDate(Date.valueOf("2026-06-01"));
        juneReport.setEndDate(Date.valueOf("2026-06-30"));

        Activity manualActivity = new Activity();
        manualActivity.setActivityId(4);
        manualActivity.setTitle("Manual de usuario del sistema");
        manualActivity.setStartDate(Date.valueOf("2026-06-15"));
        manualActivity.setEndDate(Date.valueOf("2026-06-15"));

        List<Activity> juneActivities = List.of(manualActivity);

        assertDoesNotThrow(() -> reportManager.createReportAndLinkActivities(juneReport, juneActivities));
    }

    @Test
    void getPractitionerReports_ValidId_ReturnsExpectedList() throws ManagerException {
        List<MonthlyReport> expectedReports = new ArrayList<>();
        MonthlyReport storedReport = new MonthlyReport();
        storedReport.setReportId(STORED_REPORT_ID);
        storedReport.setPractitionerId(STORED_PRACTITIONER_ID);
        storedReport.setMonthName("Mayo");
        storedReport.setYear(2026);
        storedReport.setStartDate(Date.valueOf("2026-05-01"));
        storedReport.setEndDate(Date.valueOf("2026-05-31"));
        storedReport.setStatus("Borrador");
        expectedReports.add(storedReport);

        List<MonthlyReport> resultReports = reportManager.getPractitionerReports(STORED_PRACTITIONER_ID);

        assertEquals(expectedReports, resultReports);
    }

    @Test
    void getReportsForEvaluation_ReturnsExpectedList() throws ManagerException {
        List<MonthlyReport> expectedReports = new ArrayList<>();

        List<MonthlyReport> resultReports = reportManager.getReportsForEvaluation();

        assertEquals(expectedReports, resultReports);
    }

    @Test
    void evaluateReport_ValidData_DoesNotThrow() {
        assertDoesNotThrow(() -> reportManager.evaluateReport(STORED_REPORT_ID, 10.0, "Excelente trabajo"));
    }
}
