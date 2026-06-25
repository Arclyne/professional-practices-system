package mx.uv.fei.domain.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.CoveredReport;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.Gender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ReportPdfGeneratorTest {

    @TempDir
    Path temporaryHomeDirectory;

    private ReportPdfGenerator pdfGenerator;
    private String originalUserHome;

    @BeforeEach
    void setUp() throws IOException {
        pdfGenerator = new ReportPdfGenerator();
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", temporaryHomeDirectory.toAbsolutePath().toString());
        Files.createDirectories(temporaryHomeDirectory.resolve("Downloads"));
    }

    @AfterEach
    void tearDown() {
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void generateMonthlyReportPdf_ValidData_CreatesPdfSuccessfully() {
        User practitioner = new User();
        practitioner.setName("Angel Gabriel");
        practitioner.setLastName("Aguilar Hernandez");
        practitioner.setGender(Gender.MALE);

        MonthlyReport juneReport = new MonthlyReport();
        juneReport.setMonthName("Junio");
        juneReport.setYear(2026);
        juneReport.setStartDate(Date.valueOf("2026-06-01"));
        juneReport.setEndDate(Date.valueOf("2026-06-30"));

        Activity testingActivity = new Activity();
        testingActivity.setTitle("Desarrollo de pruebas unitarias");
        testingActivity.setStartDate(Date.valueOf("2026-06-15"));
        testingActivity.setEndDate(Date.valueOf("2026-06-18"));
        testingActivity.setDurationHours(5);

        List<Activity> juneActivities = new ArrayList<>();
        juneActivities.add(testingActivity);

        assertDoesNotThrow(() -> pdfGenerator.generateMonthlyReportPdf(juneReport, practitioner, juneActivities));
    }

    @Test
    void generateProgressReportPdf_WithCoveredReports_CreatesPdfSuccessfully() {
        User practitioner = new User();
        practitioner.setName("Angel Gabriel");
        practitioner.setLastName("Aguilar Hernandez");
        practitioner.setGender(Gender.MALE);

        ProgressReport intermediateReport = new ProgressReport();
        intermediateReport.setReportType("Intermedio");
        intermediateReport.setPeriodCoveredStart(Date.valueOf("2026-05-01"));
        intermediateReport.setPeriodCoveredEnd(Date.valueOf("2026-06-30"));
        intermediateReport.setTotalHoursAtSubmission(215.0);

        MonthlyReport mayReport = new MonthlyReport();
        mayReport.setMonthName("Mayo");
        mayReport.setYear(2026);
        mayReport.setStartDate(Date.valueOf("2026-05-01"));
        mayReport.setEndDate(Date.valueOf("2026-05-31"));

        Activity mayActivity = new Activity();
        mayActivity.setTitle("Levantamiento de requisitos");
        mayActivity.setStartDate(Date.valueOf("2026-05-01"));
        mayActivity.setEndDate(Date.valueOf("2026-05-03"));
        mayActivity.setDurationHours(8);

        List<CoveredReport> coveredReports = new ArrayList<>();
        coveredReports.add(new CoveredReport(mayReport, List.of(mayActivity)));

        assertDoesNotThrow(() -> pdfGenerator.generateProgressReportPdf(intermediateReport, practitioner, coveredReports));
    }
}
