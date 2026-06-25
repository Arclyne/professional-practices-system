package mx.uv.fei.domain.manager.reporting;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IMonthlyReportDAO;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.dataaccess.interfaces.IProgressReportDAO;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PracticeAccessManager;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
public class ProgressReportManager {

    private final IProgressReportDAO progressReportDAO;
    private final IPostulationDAO postulationDAO;
    private final IMonthlyReportDAO monthlyReportDAO;
    private final PracticeAccessManager practiceAccessManager;

    @Inject
    public ProgressReportManager(IProgressReportDAO progressReportDAO, IPostulationDAO postulationDAO,
                                 IMonthlyReportDAO monthlyReportDAO, PracticeAccessManager practiceAccessManager) {
        this.progressReportDAO = progressReportDAO;
        this.postulationDAO = postulationDAO;
        this.monthlyReportDAO = monthlyReportDAO;
        this.practiceAccessManager = practiceAccessManager;
    }

    public ProgressReport generateProgressReport(int practitionerId, ProgressReportType reportType,
                                                 Date periodStart, Date periodEnd) throws ManagerException {
        practiceAccessManager.ensureSubmissionsAllowed(practitionerId);
        BaseValidator.validateStartBeforeEnd(periodStart, periodEnd,
                "La fecha de inicio del periodo debe ser anterior a la fecha de fin.");
        validateHasAssignedProject(practitionerId);
        validateCoveredReportsAreEvaluated(practitionerId, periodStart, periodEnd);
        double accumulatedHours = getAccumulatedHoursInRange(practitionerId, periodStart, periodEnd);
        validateHoursRequirement(reportType, accumulatedHours);
        validateReportDoesNotExist(practitionerId, reportType);
        validateIntermediateReportApprovedBeforeFinal(practitionerId, reportType);

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
        practiceAccessManager.ensureSubmissionsAllowed(practitionerId);
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

    public List<ProgressReport> getSubmittedProgressReports(int professorId, int periodId) throws ManagerException {
        try {
            return progressReportDAO.getSubmittedProgressReportsByProfessor(professorId, periodId);
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar los reportes de avance.", e);
        }
    }

    public double getAccumulatedHours(int practitionerId) throws ManagerException {
        try {
            return progressReportDAO.getTotalAccumulatedHours(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar los reportes de avance.", e);
        }
    }

    private double getAccumulatedHoursInRange(int practitionerId, Date periodStart, Date periodEnd)
            throws ManagerException {
        try {
            return progressReportDAO.getAccumulatedHoursInRange(practitionerId, periodStart, periodEnd);
        } catch (DAOException e) {
            throw new ManagerException("Error al calcular las horas del periodo cubierto.", e);
        }
    }

    private void validateCoveredReportsAreEvaluated(int practitionerId, Date periodStart, Date periodEnd)
            throws ManagerException {
        List<MonthlyReport> coveredReports = retrieveCoveredReports(practitionerId, periodStart, periodEnd);
        if (coveredReports.isEmpty()) {
            throw new ManagerException("No hay reportes mensuales dentro del periodo cubierto. "
                    + "Genera y haz evaluar tus reportes mensuales antes de generar el reporte de avance.");
        }
        for (MonthlyReport coveredReport : coveredReports) {
            requireEvaluatedReport(coveredReport);
        }
    }

    private void requireEvaluatedReport(MonthlyReport coveredReport) throws ManagerException {
        if (!ReportStatus.EVALUATED.getDatabaseValue().equals(coveredReport.getStatus())) {
            throw new ManagerException("El reporte mensual de " + coveredReport.getMonthName() + " "
                    + coveredReport.getYear() + " aún no ha sido evaluado por el profesor. "
                    + "Todos los reportes del periodo cubierto deben estar evaluados.");
        }
    }

    private List<MonthlyReport> retrieveCoveredReports(int practitionerId, Date periodStart, Date periodEnd)
            throws ManagerException {
        try {
            return monthlyReportDAO.getReportsByPractitionerInRange(practitionerId, periodStart, periodEnd);
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar los reportes mensuales del periodo cubierto.", e);
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

    private void validateIntermediateReportApprovedBeforeFinal(int practitionerId, ProgressReportType reportType)
            throws ManagerException {
        if (reportType == ProgressReportType.FINAL && !isIntermediateReportApproved(practitionerId)) {
            throw new ManagerException(
                    "Debes tener tu reporte Intermedio aprobado por el profesor antes de generar el reporte Final.");
        }
    }

    private boolean isIntermediateReportApproved(int practitionerId) throws ManagerException {
        boolean isApproved = false;

        try {
            ProgressReport intermediateReport = progressReportDAO
                    .getProgressReportByPractitionerAndType(practitionerId, ProgressReportType.INTERMEDIO.getDatabaseValue());
            isApproved = intermediateReport != null
                    && ReportStatus.EVALUATED.getDatabaseValue().equals(intermediateReport.getStatus());
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar los reportes de avance.", e);
        }

        return isApproved;
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
            progressReportDAO.updateProgressReport(report, report.getReportId());
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