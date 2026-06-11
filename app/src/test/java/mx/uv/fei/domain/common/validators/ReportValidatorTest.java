package mx.uv.fei.domain.common.validators;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Date;
import java.util.ArrayList;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

public class ReportValidatorTest {

    @Test
    void validateMonthlyReportCreation_InvalidDateMonthMismatch_ThrowsManagerException() {
        MonthlyReport mismatchedReport = new MonthlyReport();
        mismatchedReport.setMonthName("Enero");
        mismatchedReport.setYear(2026);
        mismatchedReport.setStartDate(Date.valueOf("2026-06-01"));
        mismatchedReport.setEndDate(Date.valueOf("2026-06-30"));

        assertThrows(ManagerException.class,
                () -> ReportValidator.validateMonthlyReportCreation(mismatchedReport, new ArrayList<>()));
    }

    @Test
    void validateLogbookActivity_InvalidDuration_ThrowsManagerException() {
        Activity zeroDurationActivity = new Activity();
        zeroDurationActivity.setTitle("Levantamiento de requisitos");
        zeroDurationActivity.setDescription("Entrevistas con el personal del area de sistemas.");
        zeroDurationActivity.setActivityDate(Date.valueOf("2026-06-01"));
        zeroDurationActivity.setDurationHours(0);

        assertThrows(ManagerException.class, () -> ReportValidator.validateLogbookActivity(zeroDurationActivity));
    }
}
