package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.*;
import mx.uv.fei.domain.exceptions.ManagerException;
import java.util.List;

public class ReportValidator {
    public static void validateSignedReport(String signedFileUrl) throws ManagerException {
        BaseValidator.validateString(signedFileUrl, "No se detectó el documento. El archivo PDF es obligatorio.");
    }

    public static void validateLogbookActivity(Activity activity) throws ManagerException {
        BaseValidator.validateString(activity.getTitle(), "El título de la actividad es obligatorio.");
        BaseValidator.validateString(activity.getDescription(), "La descripción es obligatoria.");
        if (activity.getActivityDate() == null) {
            throw new ManagerException("Debes seleccionar la fecha de la actividad.");
        }
        if (activity.getDurationHours() <= 0) {
            throw new ManagerException("La duración de la actividad debe ser mayor a 0 horas.");
        }
    }

    public static void validateMonthlyReportCreation(MonthlyReport report, List<Activity> activities) throws ManagerException {
        BaseValidator.validateString(report.getMonthName(), "Debes seleccionar un mes para el reporte.");
        if (report.getYear() <= 0) {
            throw new ManagerException("El año del reporte debe ser mayor a cero.");
        }
        if (report.getStartDate() == null || report.getEndDate() == null) {
            throw new ManagerException("Las fechas de inicio y fin son obligatorias.");
        }
        BaseValidator.validateDateRange(report.getStartDate(), report.getEndDate(), "The end date cannot be earlier than the start date.");
        validateReportDatesMatchMonthAndYear(report);

        if (activities == null || activities.isEmpty()) {
            throw new ManagerException("Debes seleccionar al menos una actividad libre para generar el reporte.");
        }

        for (Activity activity : activities) {
            if (activity.getActivityDate().before(report.getStartDate()) || activity.getActivityDate().after(report.getEndDate())) {
                throw new ManagerException("La actividad '" + activity.getTitle() + "' (" + activity.getActivityDate() + ") está fuera del rango de fechas del reporte.");
            }
        }
    }

    private static void validateReportDatesMatchMonthAndYear(MonthlyReport report) throws ManagerException {
        int expectedMonth = getMonthNumber(report.getMonthName());
        java.time.LocalDate startLocal = report.getStartDate().toLocalDate();
        java.time.LocalDate endLocal = report.getEndDate().toLocalDate();
        boolean isStartValid = (startLocal.getMonthValue() == expectedMonth && startLocal.getYear() == report.getYear());
        boolean isEndValid = (endLocal.getMonthValue() == expectedMonth && endLocal.getYear() == report.getYear());
        if (!isStartValid || !isEndValid) {
            throw new ManagerException("Las fechas seleccionadas no coinciden con el mes y año especificados para el reporte.");
        }
    }

    private static int getMonthNumber(String monthName) {
        return switch (monthName.toLowerCase()) {
            case "enero" -> 1; case "febrero" -> 2; case "marzo" -> 3;
            case "abril" -> 4; case "mayo" -> 5; case "junio" -> 6;
            case "julio" -> 7; case "agosto" -> 8; case "septiembre" -> 9;
            case "octubre" -> 10; case "noviembre" -> 11; case "diciembre" -> 12;
            default -> -1;
        };
    }

    public static void validateReportEvaluation(Double grade, String feedback) throws ManagerException {
        if (grade == null) {
            throw new ManagerException("La calificación es obligatoria.");
        }
        if (grade < 0.0 || grade > 10.0) {
            throw new ManagerException("La calificación debe ser un valor entre 0 y 10.");
        }
        BaseValidator.validateString(feedback, "La retroalimentación para el practicante es obligatoria.");
    }
}
