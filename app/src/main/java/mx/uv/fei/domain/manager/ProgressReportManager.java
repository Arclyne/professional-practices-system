package mx.uv.fei.domain.manager;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IProgressReportDAO;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;


@Component
public class ProgressReportManager {

    private static final String MSG_INSUFFICIENT_HOURS_INTERMEDIATE =
            "El reporte intermedio requiere al menos 210 horas acumuladas. " +
            "Horas actuales: %.1f";
    private static final String MSG_INSUFFICIENT_HOURS_FINAL =
            "El reporte final requiere al menos 420 horas acumuladas. " +
            "Horas actuales: %.1f";
    private static final String MSG_ALREADY_EXISTS =
            "Ya existe un reporte de tipo '%s' para este practicante.";
    private static final String MSG_INSERT_ERROR =
            "No se pudo generar el reporte de avance.";
    private static final String MSG_RETRIEVE_ERROR =
            "Error al recuperar los reportes de avance.";
    private static final String MSG_UPDATE_ERROR =
            "No se pudo actualizar el reporte de avance.";
    private static final String MSG_NOT_FOUND =
            "No se encontró un reporte de tipo '%s' para este practicante.";
    private static final String MSG_INVALID_FILE =
            "El archivo firmado es obligatorio para enviar el reporte.";

    private final IProgressReportDAO progressReportDAO;

    @Inject
    public ProgressReportManager(IProgressReportDAO progressReportDAO) {
        this.progressReportDAO = progressReportDAO;
    }


    public ProgressReport generateProgressReport(
            int practitionerId,
            ProgressReportType reportType,
            Date periodStart,
            Date periodEnd) throws ManagerException {

        double accumulatedHours = getAccumulatedHours(practitionerId);

        validateHoursRequirement(reportType, accumulatedHours);
        validateReportDoesNotExist(practitionerId, reportType);

        ProgressReport report = buildProgressReport(
                practitionerId, reportType, periodStart, periodEnd, accumulatedHours);

        try {
            int generatedId = progressReportDAO.insertProgressReport(report);
            if (generatedId <= 0) {
                throw new ManagerException(MSG_INSERT_ERROR);
            }
            report.setReportId(generatedId);
        } catch (DAOException exception) {
            throw new ManagerException(MSG_INSERT_ERROR + " Causa: " + exception.getMessage(), exception);
        }

        return report;
    }

    private double getAccumulatedHours(int practitionerId) throws ManagerException {
        double accumulatedHours;

        try {
            accumulatedHours = progressReportDAO.getTotalAccumulatedHours(practitionerId);
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, exception);
        }

        return accumulatedHours;
    }

    private void validateHoursRequirement(ProgressReportType reportType, double accumulatedHours) throws ManagerException {
        boolean hasSufficientHours = accumulatedHours >= reportType.getRequiredHours();

        if (!hasSufficientHours) {
            String errorMessage = reportType == ProgressReportType.INTERMEDIO
                    ? String.format(MSG_INSUFFICIENT_HOURS_INTERMEDIATE, accumulatedHours)
                    : String.format(MSG_INSUFFICIENT_HOURS_FINAL, accumulatedHours);
            throw new ManagerException(errorMessage);
        }
    }

    private void validateReportDoesNotExist(int practitionerId, ProgressReportType reportType) throws ManagerException {
        try {
            ProgressReport existing = progressReportDAO
                    .getProgressReportByPractitionerAndType(practitionerId, reportType.getDatabaseValue());

            if (existing != null) {
                throw new ManagerException(String.format(MSG_ALREADY_EXISTS, reportType.getDatabaseValue()));
            }
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, exception);
        }
    }

    private ProgressReport buildProgressReport(
            int practitionerId,
            ProgressReportType reportType,
            Date periodStart,
            Date periodEnd,
            double totalHours) {

        ProgressReport report = new ProgressReport();
        report.setPractitionerId(practitionerId);
        report.setReportType(reportType.getDatabaseValue());
        report.setGenerationDate(Date.valueOf(LocalDate.now()));
        report.setPeriodCoveredStart(periodStart);
        report.setPeriodCoveredEnd(periodEnd);
        report.setTotalHoursAtSubmission(totalHours);
        report.setStatus(ReportStatus.PENDING.getDatabaseValue());

        return report;
    }


    public void submitSignedProgressReport(
            int practitionerId,
            ProgressReportType reportType,
            String signedFileUrl) throws ManagerException {

        if (signedFileUrl == null || signedFileUrl.trim().isEmpty()) {
            throw new ManagerException(MSG_INVALID_FILE);
        }

        ProgressReport report = getExistingReport(practitionerId, reportType);

        report.setSignedFileUrl(signedFileUrl.trim());
        report.setStatus(ReportStatus.SUBMITTED.getDatabaseValue());

        updateReport(report);
    }

    private ProgressReport getExistingReport(int practitionerId, ProgressReportType reportType) throws ManagerException {
        ProgressReport report;

        try {
            report = progressReportDAO
                    .getProgressReportByPractitionerAndType(practitionerId, reportType.getDatabaseValue());
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, exception);
        }

        if (report == null) {
            throw new ManagerException(String.format(MSG_NOT_FOUND, reportType.getDatabaseValue()));
        }

        return report;
    }

    private void updateReport(ProgressReport report) throws ManagerException {
        try {
            boolean isUpdated = progressReportDAO.updateProgressReport(report, report.getReportId());
            if (!isUpdated) {
                throw new ManagerException(MSG_UPDATE_ERROR);
            }
        } catch (DAOException exception) {
            throw new ManagerException(MSG_UPDATE_ERROR + " Causa: " + exception.getMessage(), exception);
        }
    }

    public void evaluateProgressReport(
            int practitionerId,
            ProgressReportType reportType,
            double grade,
            String feedback) throws ManagerException {

        validateGrade(grade);
        validateFeedback(feedback);

        ProgressReport report = getExistingReport(practitionerId, reportType);

        report.setGrade(grade);
        report.setProfessorFeedback(feedback.trim());
        report.setStatus(ReportStatus.EVALUATED.getDatabaseValue());

        updateReport(report);
    }

    private void validateGrade(double grade) throws ManagerException {
        boolean isValidGrade = grade >= 0.0 && grade <= 10.0;

        if (!isValidGrade) {
            throw new ManagerException("La calificación debe ser un valor entre 0 y 10.");
        }
    }

    private void validateFeedback(String feedback) throws ManagerException {
        boolean isFeedbackEmpty = (feedback == null || feedback.trim().isEmpty());

        if (isFeedbackEmpty) {
            throw new ManagerException("La retroalimentación es obligatoria al evaluar el reporte.");
        }
    }

    public List<ProgressReport> getProgressReportsByPractitioner(int practitionerId) throws ManagerException {
        List<ProgressReport> reports;

        try {
            reports = progressReportDAO.getProgressReportsByPractitioner(practitionerId);
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, exception);
        }

        return reports;
    }

    public List<ProgressReport> getSubmittedProgressReports() throws ManagerException {
        List<ProgressReport> reports;

        try {
            reports = progressReportDAO.getSubmittedProgressReports();
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, exception);
        }

        return reports;
    }
}
