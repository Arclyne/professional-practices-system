package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.*;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.Date;
import java.util.ArrayList;

public class ReportValidatorTest {

    @Test
    void validateMonthlyReportCreation_InvalidDateMonthMismatch_ThrowsManagerException() {
        MonthlyReport report = new MonthlyReport();
        report.setMonthName("Enero");
        report.setYear(2026);
        report.setStartDate(Date.valueOf("2026-06-01"));
        report.setEndDate(Date.valueOf("2026-06-30"));

        assertThrows(ManagerException.class, () ->
                ReportValidator.validateMonthlyReportCreation(report, new ArrayList<>()));
    }

    @Test
    void validateLogbookActivity_InvalidDuration_ThrowsManagerException() {
        Activity activity = new Activity();
        activity.setTitle("Title");
        activity.setDescription("Desc");
        activity.setActivityDate(Date.valueOf("2026-06-01"));
        activity.setDurationHours(0);

        assertThrows(ManagerException.class, () -> ReportValidator.validateLogbookActivity(activity));
    }
}