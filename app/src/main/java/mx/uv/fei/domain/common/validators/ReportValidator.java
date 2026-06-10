package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.enums.Month;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.time.LocalDate;
import java.util.List;

public class ReportValidator {

    private static final double MINIMUM_GRADE = 0.0;
    private static final double MAXIMUM_GRADE = 10.0;

    public static void validateSignedReport(String signedFileUrl) throws ManagerException {
        BaseValidator.validateString(signedFileUrl,
                "No se detectó el documento. El archivo PDF es obligatorio.");
    }

    public static void validateLogbookActivity(Activity activity) throws ManagerException {
        BaseValidator.validateString(activity.getTitle(),
                "El título de la actividad es obligatorio.");
        BaseValidator.validateString(activity.getDescription(),
                "La descripción es obligatoria.");
        if (activity.getActivityDate() == null) {
            throw new ManagerException("Debe seleccionar la fecha de la actividad.");
        }
        if (activity.getDurationHours() <= 0) {
            throw new ManagerException("La duración de la actividad debe ser mayor a 0 horas.");
        }
    }

    public static void validateMonthlyReportCreation(MonthlyReport report, List<Activity> activities) throws ManagerException {
        BaseValidator.validateString(report.getMonthName(),
                "Debe seleccionar un mes para el reporte.");
        if (report.getYear() <= 0) {
            throw new ManagerException("El año del reporte debe ser mayor a cero.");
        }
        if (report.getStartDate() == null || report.getEndDate() == null) {
            throw new ManagerException("Las fechas de inicio y fin son obligatorias.");
        }
        BaseValidator.validateDateRange(report.getStartDate(), report.getEndDate(),
                "La fecha de fin no puede ser anterior a la fecha de inicio.");
        validateReportDatesMatchMonthAndYear(report);
        validateActivitiesWithinReportRange(report, activities);
    }

    public static void validateReportEvaluation(Double grade, String feedback) throws ManagerException {
        if (grade == null) {
            throw new ManagerException("La calificación es obligatoria.");
        }
        if (grade < MINIMUM_GRADE || grade > MAXIMUM_GRADE) {
            throw new ManagerException("La calificación debe ser un valor entre 0 y 10.");
        }
        BaseValidator.validateString(feedback,
                "La retroalimentación para el practicante es obligatoria.");
    }

    private static void validateActivitiesWithinReportRange(MonthlyReport report, List<Activity> activities) throws ManagerException {
        if (activities == null || activities.isEmpty()) {
            throw new ManagerException("Debe seleccionar al menos una actividad libre para generar el reporte.");
        }
        for (Activity activity : activities) {
            if (activity.getActivityDate().before(report.getStartDate()) || activity.getActivityDate().after(report.getEndDate())) {
                throw new ManagerException("La actividad '" + activity.getTitle() + "' (" + activity.getActivityDate() + ") está fuera del rango de fechas del reporte.");
            }
        }
    }

    private static void validateReportDatesMatchMonthAndYear(MonthlyReport report) throws ManagerException {
        Month expectedMonth = Month.fromString(report.getMonthName());
        LocalDate startDate = report.getStartDate().toLocalDate();
        LocalDate endDate = report.getEndDate().toLocalDate();
        boolean isStartDateValid = startDate.getMonthValue() == expectedMonth.getMonthNumber() && startDate.getYear() == report.getYear();
        boolean isEndDateValid = endDate.getMonthValue() == expectedMonth.getMonthNumber() && endDate.getYear() == report.getYear();
        if (!isStartDateValid || !isEndDateValid) {
            throw new ManagerException("Las fechas seleccionadas no coinciden con el mes y año especificados para el reporte.");
        }
    }
}