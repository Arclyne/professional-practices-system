package mx.uv.fei.domain.manager.reporting;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IMonthlyReportDAO;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.domain.common.PersistenceErrorTranslator;
import mx.uv.fei.domain.common.validators.ReportValidator;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.CoveredReport;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.enums.DocumentCategory;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PracticeAccessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Component
public class MonthlyReportManager {

    private static final Logger log = LoggerFactory.getLogger(MonthlyReportManager.class);

    private final IMonthlyReportDAO reportDAO;
    private final IActivityDAO activityDAO;
    private final IPostulationDAO postulationDAO;
    private final IPractitionerDocumentDAO documentDAO;
    private final PracticeAccessManager practiceAccessManager;

    @Inject
    public MonthlyReportManager(IMonthlyReportDAO reportDAO, IActivityDAO activityDAO, IPostulationDAO postulationDAO,
                                IPractitionerDocumentDAO documentDAO, PracticeAccessManager practiceAccessManager) {
        this.reportDAO = reportDAO;
        this.activityDAO = activityDAO;
        this.postulationDAO = postulationDAO;
        this.documentDAO = documentDAO;
        this.practiceAccessManager = practiceAccessManager;
    }

    public void createReportAndLinkActivities(MonthlyReport report, List<Activity> selectedActivities) throws ManagerException {
        practiceAccessManager.ensureSubmissionsAllowed(report.getPractitionerId());
        validateHasAssignedProject(report.getPractitionerId());
        validateAllInitialDocumentsAccepted(report.getPractitionerId());
        ReportValidator.validateMonthlyReportCreation(report, selectedActivities);
        validateNoReportForSameMonth(report);

        try {
            int generatedReportId = reportDAO.insertReport(report);
            if (generatedReportId <= 0) {
                throw new ManagerException("No se pudo generar el reporte mensual.");
            }
            linkActivitiesToReport(selectedActivities, generatedReportId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw PersistenceErrorTranslator.translate(e);
        }
    }

    public List<MonthlyReport> getPractitionerReports(int practitionerId) throws ManagerException {
        try {
            return reportDAO.getReportsByPractitioner(practitionerId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al cargar los reportes.", e);
        }
    }

    public List<CoveredReport> getEvaluatedReportsWithActivitiesInRange(int practitionerId, Date startDate,
                                                                        Date endDate) throws ManagerException {
        try {
            List<MonthlyReport> reportsInRange = reportDAO.getReportsByPractitionerInRange(practitionerId, startDate, endDate);
            List<CoveredReport> coveredReports = new ArrayList<>();
            for (MonthlyReport report : reportsInRange) {
                addEvaluatedReportWithActivities(coveredReports, report);
            }
            return coveredReports;
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al cargar los reportes del periodo cubierto.", e);
        }
    }

    private void addEvaluatedReportWithActivities(List<CoveredReport> coveredReports, MonthlyReport report)
            throws DAOException {
        if (ReportStatus.EVALUATED.getDatabaseValue().equals(report.getStatus())) {
            List<Activity> activities = activityDAO.getActivitiesByReport(report.getReportId());
            coveredReports.add(new CoveredReport(report, activities));
        }
    }

    public void submitSignedReport(MonthlyReport report, String signedFileUrl) throws ManagerException {
        practiceAccessManager.ensureSubmissionsAllowed(report.getPractitionerId());
        validateHasAssignedProject(report.getPractitionerId());
        ReportValidator.validateSignedReport(signedFileUrl);
        report.setSignedFileUrl(signedFileUrl);
        report.setStatus(ReportStatus.SUBMITTED.getDatabaseValue());

        try {
            reportDAO.updateReport(report, report.getReportId());
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al enviar el reporte firmado.", e);
        }
    }

    public List<MonthlyReport> getReportsForEvaluation() throws ManagerException {
        try {
            return reportDAO.getSubmittedReports();
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al cargar los reportes para evaluar.", e);
        }
    }

    public List<MonthlyReport> getReportsForEvaluation(int professorId, int periodId) throws ManagerException {
        try {
            return reportDAO.getSubmittedReportsByProfessor(professorId, periodId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al cargar los reportes para evaluar.", e);
        }
    }

    public void evaluateReport(int reportId, Double grade, String feedback) throws ManagerException {
        ReportValidator.validateReportEvaluation(grade, feedback);

        try {
            MonthlyReport report = reportDAO.getReportById(reportId);
            if (report == null || report.getReportId() <= 0) {
                throw new ManagerException("El reporte especificado no existe.");
            }
            report.setGrade(grade);
            report.setProfessorFeedback(feedback.trim());
            report.setStatus(ReportStatus.EVALUATED.getDatabaseValue());

            reportDAO.updateReport(report, reportId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al intentar guardar la evaluación.", e);
        }
    }

    public void rejectReport(int reportId, String feedback) throws ManagerException {
        ReportValidator.validateRejectionFeedback(feedback);

        try {
            MonthlyReport report = reportDAO.getReportById(reportId);
            if (report == null || report.getReportId() <= 0) {
                throw new ManagerException("El reporte especificado no existe.");
            }
            report.setProfessorFeedback(feedback.trim());
            report.setStatus(ReportStatus.REJECTED.getDatabaseValue());

            reportDAO.updateReport(report, reportId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al intentar rechazar el reporte.", e);
        }
    }

    public boolean verifyHasAssignedProject(int practitionerId) throws ManagerException {
        try {
            return postulationDAO.hasAssignedProject(practitionerId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo verificar el estado del proyecto asignado.", e);
        }
    }

    public boolean verifyAllInitialDocumentsAccepted(int practitionerId) throws ManagerException {
        try {
            return documentDAO.areAllDocumentsAccepted(practitionerId, DocumentCategory.INITIAL.getDatabaseValue());
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudieron verificar los documentos iniciales del practicante.", e);
        }
    }

    private void validateNoReportForSameMonth(MonthlyReport report) throws ManagerException {
        try {
            List<MonthlyReport> existingReports = reportDAO.getReportsByPractitioner(report.getPractitionerId());
            boolean alreadyHasReportForMonth = existingReports.stream().anyMatch(existingReport ->
                    existingReport.getYear() == report.getYear()
                            && existingReport.getMonthName() != null
                            && existingReport.getMonthName().equalsIgnoreCase(report.getMonthName()));

            if (alreadyHasReportForMonth) {
                throw new ManagerException("Ya tienes un reporte registrado para "
                        + report.getMonthName() + " " + report.getYear() + ".");
            }
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo verificar si ya existe un reporte para ese mes.", e);
        }
    }

    private void validateAllInitialDocumentsAccepted(int practitionerId) throws ManagerException {
        if (!verifyAllInitialDocumentsAccepted(practitionerId)) {
            throw new ManagerException("Aún no puedes registrar reportes. "
                    + "Tu profesor debe aceptar todos tus documentos iniciales primero.");
        }
    }

    private void validateHasAssignedProject(int practitionerId) throws ManagerException {
        try {
            boolean hasAssignedProject = postulationDAO.hasAssignedProject(practitionerId);
            if (!hasAssignedProject) {
                throw new ManagerException("No puedes crear o enviar reportes sin tener un proyecto asignado. Postula a un proyecto primero.");
            }
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo verificar el estado del proyecto asignado.", e);
        }
    }

    private void linkActivitiesToReport(List<Activity> activities, int reportId) throws DAOException {
        if (activities == null || activities.isEmpty()) {
            return;
        }
        for (Activity activity : activities) {
            activityDAO.assignActivityToReport(activity.getActivityId(), reportId);
        }
    }
}