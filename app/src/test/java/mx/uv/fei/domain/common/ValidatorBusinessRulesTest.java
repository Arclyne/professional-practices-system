package mx.uv.fei.domain.common;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.domain.common.Validator;
import org.junit.jupiter.api.Test;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.exceptions.ManagerException;

import static org.junit.jupiter.api.Assertions.*;

public class ValidatorBusinessRulesTest {

    @Test
    void validateMonthlyReportCreation_InvalidDateMonthMismatch_ThrowsManagerException() {
        MonthlyReport report = new MonthlyReport();
        report.setMonthName("Enero");
        report.setYear(2026);
        report.setStartDate(Date.valueOf("2026-06-01"));
        report.setEndDate(Date.valueOf("2026-06-30"));
        List<Activity> activities = new ArrayList<>();

        ManagerException e = assertThrows(ManagerException.class,
                () -> Validator.validateMonthlyReportCreation(report, activities));

        assertEquals("Las fechas seleccionadas no coinciden con el mes y año especificados para el reporte.", e.getMessage());
    }

    @Test
    void validateMonthlyReportCreation_ActivityOutOfRange_ThrowsManagerException() {
        MonthlyReport report = new MonthlyReport();
        report.setMonthName("Junio");
        report.setYear(2026);
        report.setStartDate(Date.valueOf("2026-06-01"));
        report.setEndDate(Date.valueOf("2026-06-30"));

        Activity outOfRangeActivity = new Activity();
        outOfRangeActivity.setTitle("Actividad Inválida");
        outOfRangeActivity.setActivityDate(Date.valueOf("2026-04-01"));
        List<Activity> activities = List.of(outOfRangeActivity);

        ManagerException e = assertThrows(ManagerException.class,
                () -> Validator.validateMonthlyReportCreation(report, activities));

        assertTrue(e.getMessage().contains("está fuera del rango de fechas"));
    }
}