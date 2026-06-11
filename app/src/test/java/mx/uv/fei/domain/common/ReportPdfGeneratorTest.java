package mx.uv.fei.domain.common;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
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
        testingActivity.setActivityDate(Date.valueOf("2026-06-15"));
        testingActivity.setDurationHours(5);

        List<Activity> juneActivities = new ArrayList<>();
        juneActivities.add(testingActivity);

        assertDoesNotThrow(() -> pdfGenerator.generateMonthlyReportPdf(juneReport, practitioner, juneActivities));
    }
}
