package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.enums.Month;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReportValidator {

    private static final double MINIMUM_GRADE = 0.0;
    private static final double MAXIMUM_GRADE = 10.0;
    private static final int MAX_HOURS_PER_DAY = 12;

    public static void validateSignedReport(String signedFileUrl) throws ManagerException {
        BaseValidator.validateString(signedFileUrl,
                "No se detectó el documento. El archivo PDF es obligatorio.");
        BaseValidator.validateMaxLength(signedFileUrl, FieldLengthLimits.SIGNED_FILE_URL_MAX,
                "La ruta del documento firmado no puede exceder " + FieldLengthLimits.SIGNED_FILE_URL_MAX + " caracteres.");
    }

    public static void validateLogbookActivity(Activity activity) throws ManagerException {
        BaseValidator.validateString(activity.getTitle(),
                "El título de la actividad es obligatorio.");
        BaseValidator.validateMaxLength(activity.getTitle(), FieldLengthLimits.ACTIVITY_TITLE_MAX,
                "El título de la actividad no puede exceder " + FieldLengthLimits.ACTIVITY_TITLE_MAX + " caracteres.");
        BaseValidator.validateString(activity.getDescription(),
                "La descripción es obligatoria.");
        BaseValidator.validateMaxLength(activity.getDescription(), FieldLengthLimits.LONG_TEXT_MAX,
                "La descripción de la actividad no puede exceder " + FieldLengthLimits.LONG_TEXT_MAX + " caracteres.");
        validateActivityDateRange(activity);
        if (activity.getDurationHours() <= 0) {
            throw new ManagerException("La duración de la actividad debe ser mayor a 0 horas.");
        }
        validateActivityDurationWithinDailyLimit(activity);
    }

    private static void validateActivityDateRange(Activity activity) throws ManagerException {
        if (activity.getStartDate() == null || activity.getEndDate() == null) {
            throw new ManagerException("Debe seleccionar la fecha de inicio y la fecha de fin de la actividad.");
        }
        if (activity.getStartDate().after(activity.getEndDate())) {
            throw new ManagerException("La fecha de inicio de la actividad no puede ser posterior a la fecha de fin.");
        }
    }

    private static void validateActivityDurationWithinDailyLimit(Activity activity) throws ManagerException {
        long coveredDays = ChronoUnit.DAYS.between(
                activity.getStartDate().toLocalDate(), activity.getEndDate().toLocalDate()) + 1;
        long maximumHours = coveredDays * MAX_HOURS_PER_DAY;
        if (activity.getDurationHours() > maximumHours) {
            throw new ManagerException("La duración de la actividad no puede exceder " + maximumHours
                    + " horas para el rango de fechas seleccionado (" + MAX_HOURS_PER_DAY + " horas por día).");
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
        BaseValidator.validateMaxLength(feedback, FieldLengthLimits.LONG_TEXT_MAX,
                "La retroalimentación no puede exceder " + FieldLengthLimits.LONG_TEXT_MAX + " caracteres.");
    }

    public static void validateRejectionFeedback(String feedback) throws ManagerException {
        BaseValidator.validateString(feedback,
                "Debes indicar el motivo del rechazo del reporte.");
        BaseValidator.validateMaxLength(feedback, FieldLengthLimits.LONG_TEXT_MAX,
                "El motivo del rechazo no puede exceder " + FieldLengthLimits.LONG_TEXT_MAX + " caracteres.");
    }

    private static void validateActivitiesWithinReportRange(MonthlyReport report, List<Activity> activities) throws ManagerException {
        if (activities == null || activities.isEmpty()) {
            throw new ManagerException("Debe seleccionar al menos una actividad libre para generar el reporte.");
        }
        for (Activity activity : activities) {
            if (activity.getStartDate().before(report.getStartDate()) || activity.getEndDate().after(report.getEndDate())) {
                throw new ManagerException("La actividad '" + activity.getTitle() + "' (" + activity.getStartDate()
                        + " al " + activity.getEndDate() + ") está fuera del rango de fechas del reporte.");
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