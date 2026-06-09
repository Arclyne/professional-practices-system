package mx.uv.fei.domain.common;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.Gender;

public class ReportPdfGeneratorTest {

    private ReportPdfGenerator pdfGenerator;
    private String originalUserHome;

    @TempDir
    Path tempHomeDir;

    @BeforeEach
    void setUp() throws Exception {
        pdfGenerator = new ReportPdfGenerator();
        originalUserHome = System.getProperty("user.home");
        System.setProperty("user.home", tempHomeDir.toAbsolutePath().toString());
        Files.createDirectories(tempHomeDir.resolve("Downloads"));
    }

    @AfterEach
    void tearDown() {
        if (originalUserHome != null) {
            System.setProperty("user.home", originalUserHome);
        }
    }

    @Test
    void generatePdf_ValidData_CreatesPdfSuccessfully() {
        User practitioner = new User();
        practitioner.setName("Angel");
        practitioner.setLastName("Aguilar");
        practitioner.setGender(Gender.MALE);

        MonthlyReport report = new MonthlyReport();
        report.setMonthName("Junio");
        report.setYear(2026);
        report.setStartDate(Date.valueOf("2026-06-01"));
        report.setEndDate(Date.valueOf("2026-06-30"));

        Activity activity = new Activity();
        activity.setTitle("Desarrollo de pruebas unitarias");
        activity.setActivityDate(Date.valueOf("2026-06-15"));
        activity.setDurationHours(5);

        List<Activity> activities = new ArrayList<>();
        activities.add(activity);

        assertDoesNotThrow(() -> pdfGenerator.generatePdf(report, practitioner, activities));
    
    }
}
