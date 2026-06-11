package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.dataaccess.interfaces.IProgressReportDAO;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
public class ProgressReportManager {

    private final IProgressReportDAO progressReportDAO;
    private final IPostulationDAO postulationDAO;

    @Inject
    public ProgressReportManager(IProgressReportDAO progressReportDAO, IPostulationDAO postulationDAO) {
        this.progressReportDAO = progressReportDAO;
        this.postulationDAO = postulationDAO;
    }

    public ProgressReport generateProgressReport(int practitionerId, ProgressReportType reportType,
                                                 Date periodStart, Date periodEnd) throws ManagerException {
        validateHasAssignedProject(practitionerId);
        double accumulatedHours = getAccumulatedHours(practitionerId);
        validateHoursRequirement(reportType, accumulatedHours);
        validateReportDoesNotExist(practitionerId, reportType);

        ProgressReport report = buildProgressReport(practitionerId, reportType, periodStart, periodEnd, accumulatedHours);

        try {
            int generatedId = progressReportDAO.insertProgressReport(report);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo generar el reporte de avance.");
            }
            report.setReportId(generatedId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo generar el reporte de avance.", e);
        }

        return report;
    }

    public void submitSignedProgressReport(int practitionerId, ProgressReportType reportType,
                                           String signedFileUrl) throws ManagerException {
        if (signedFileUrl == null || signedFileUrl.trim().isEmpty()) {
            throw new ManagerException("El archivo firmado es obligatorio para enviar el reporte.");
        }
        ProgressReport report = getExistingReport(practitionerId, reportType);
        report.setSignedFileUrl(signedFileUrl.trim());
        report.setStatus(ReportStatus.SUBMITTED.getDatabaseValue());
        updateReport(report);
    }

    public void evaluateProgressReport(int practitionerId, ProgressReportType reportType,
                                       double grade, String feedback) throws ManagerException {
        validateGrade(grade);
        validateFeedback(feedback);
        ProgressReport report = getExistingReport(practitionerId, reportType);
        report.setGrade(grade);
        report.setProfessorFeedback(feedback.trim());
        report.setStatus(ReportStatus.EVALUATED.getDatabaseValue());
        updateReport(report);
    }

    public List<ProgressReport> getProgressReportsByPractitioner(int practitionerId) throws ManagerException {
        try {
            return progressReportDAO.getProgressReportsByPractitioner(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar los reportes de avance.", e);
        }
    }

    public List<ProgressReport> getSubmittedProgressReports() throws ManagerException {
        try {
            return progressReportDAO.getSubmittedProgressReports();
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar los reportes de avance.", e);
        }
    }

    private double getAccumulatedHours(int practitionerId) throws ManagerException {
        try {
            return progressReportDAO.getTotalAccumulatedHours(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar los reportes de avance.", e);
        }
    }

    private void validateHoursRequirement(ProgressReportType reportType, double accumulatedHours) throws ManagerException {
        if (accumulatedHours < reportType.getRequiredHours()) {
            throw new ManagerException(String.format(
                    "El reporte de tipo '%s' requiere al menos %.0f horas acumuladas. Horas actuales: %.1f",
                    reportType.getDatabaseValue(), (double) reportType.getRequiredHours(), accumulatedHours));
        }
    }

    private void validateReportDoesNotExist(int practitionerId, ProgressReportType reportType) throws ManagerException {
        try {
            ProgressReport existingReport = progressReportDAO
                    .getProgressReportByPractitionerAndType(practitionerId, reportType.getDatabaseValue());
            if (existingReport != null) {
                throw new ManagerException(String.format(
                        "Ya existe un reporte de tipo '%s' para este practicante.", reportType.getDatabaseValue()));
            }
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar los reportes de avance.", e);
        }
    }

    private ProgressReport buildProgressReport(int practitionerId, ProgressReportType reportType,
                                               Date periodStart, Date periodEnd, double totalHours) {
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

    private ProgressReport getExistingReport(int practitionerId, ProgressReportType reportType) throws ManagerException {
        try {
            ProgressReport report = progressReportDAO
                    .getProgressReportByPractitionerAndType(practitionerId, reportType.getDatabaseValue());
            if (report == null) {
                throw new ManagerException(String.format(
                        "No se encontró un reporte de tipo '%s' para este practicante.", reportType.getDatabaseValue()));
            }
            return report;
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar los reportes de avance.", e);
        }
    }

    private void updateReport(ProgressReport report) throws ManagerException {
        try {
            boolean isUpdated = progressReportDAO.updateProgressReport(report, report.getReportId());
            if (!isUpdated) {
                throw new ManagerException("No se pudo actualizar el reporte de avance.");
            }
        } catch (DAOException e) {
            throw new ManagerException("No se pudo actualizar el reporte de avance.", e);
        }
    }

    private void validateHasAssignedProject(int practitionerId) throws ManagerException {
        try {
            boolean hasAssignedProject = postulationDAO.hasAssignedProject(practitionerId);
            if (!hasAssignedProject) {
                throw new ManagerException("No puedes generar reportes de avance sin tener un proyecto asignado.");
            }
        } catch (DAOException e) {
            throw new ManagerException("No se pudo verificar el estado del proyecto asignado.", e);
        }
    }

    private void validateGrade(double grade) throws ManagerException {
        if (grade < 0.0 || grade > 10.0) {
            throw new ManagerException("La calificación debe ser un valor entre 0 y 10.");
        }
    }

    private void validateFeedback(String feedback) throws ManagerException {
        if (feedback == null || feedback.trim().isEmpty()) {
            throw new ManagerException("La retroalimentación es obligatoria al evaluar el reporte.");
        }
    }
}