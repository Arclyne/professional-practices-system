package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class MonthlyReportManagerTest {

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
        MonthlyReport report = new MonthlyReport();
        report.setPractitionerId(123);
        report.setMonthName("Junio");
        report.setYear(2026);
        report.setStartDate(Date.valueOf("2026-06-01"));
        report.setEndDate(Date.valueOf("2026-06-30"));

        Activity activityFromDb = new Activity();
        activityFromDb.setActivityId(4);
        activityFromDb.setTitle("Actividad Junio Valida");
        activityFromDb.setActivityDate(Date.valueOf("2026-06-15"));

        List<Activity> activities = List.of(activityFromDb);

        assertDoesNotThrow(() -> reportManager.createReportAndLinkActivities(report, activities));
    }
}