package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IMonthlyReportDAO;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.common.validators.ReportValidator;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class MonthlyReportManager {

    private final IMonthlyReportDAO reportDAO;
    private final IActivityDAO activityDAO;
    private final IPostulationDAO postulationDAO;
    private final IPractitionerDAO practitionerDAO;

    @Inject
    public MonthlyReportManager(IMonthlyReportDAO reportDAO, IActivityDAO activityDAO, IPostulationDAO postulationDAO,
                                IPractitionerDAO practitionerDAO) {
        this.reportDAO = reportDAO;
        this.activityDAO = activityDAO;
        this.postulationDAO = postulationDAO;
        this.practitionerDAO = practitionerDAO;
    }

    public void createReportAndLinkActivities(MonthlyReport report, List<Activity> selectedActivities) throws ManagerException {
        validateHasAssignedProject(report.getPractitionerId());
        validateReportsAccessGranted(report.getPractitionerId());
        ReportValidator.validateMonthlyReportCreation(report, selectedActivities);

        try {
            int generatedReportId = reportDAO.insertReport(report);
            if (generatedReportId <= 0) {
                throw new ManagerException("No se pudo generar el reporte mensual.");
            }
            linkActivitiesToReport(selectedActivities, generatedReportId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo generar el reporte mensual.", e);
        }
    }

    public List<MonthlyReport> getPractitionerReports(int practitionerId) throws ManagerException {
        try {
            return reportDAO.getReportsByPractitioner(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al cargar los reportes.", e);
        }
    }

    public void submitSignedReport(MonthlyReport report, String signedFileUrl) throws ManagerException {
        validateHasAssignedProject(report.getPractitionerId());
        ReportValidator.validateSignedReport(signedFileUrl);
        report.setSignedFileUrl(signedFileUrl);
        report.setStatus(ReportStatus.SUBMITTED.getDatabaseValue());

        try {
            reportDAO.updateReport(report, report.getReportId());
        } catch (DAOException e) {
            throw new ManagerException("Error al enviar el reporte firmado.", e);
        }
    }

    public List<MonthlyReport> getReportsForEvaluation() throws ManagerException {
        try {
            return reportDAO.getSubmittedReports();
        } catch (DAOException e) {
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
            throw new ManagerException("Ocurrió un error al intentar guardar la evaluación.", e);
        }
    }

    public boolean verifyHasAssignedProject(int practitionerId) throws ManagerException {
        try {
            return postulationDAO.hasAssignedProject(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo verificar el estado del proyecto asignado.", e);
        }
    }

    public boolean verifyReportsAccessGranted(int practitionerId) throws ManagerException {
        try {
            return practitionerDAO.isReportsAccessGranted(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo verificar el acceso a reportes del practicante.", e);
        }
    }

    private void validateReportsAccessGranted(int practitionerId) throws ManagerException {
        try {
            boolean isAccessGranted = practitionerDAO.isReportsAccessGranted(practitionerId);
            if (!isAccessGranted) {
                throw new ManagerException("Tu profesor aún no habilita el registro de reportes. "
                        + "Debe aceptar todos tus documentos iniciales primero.");
            }
        } catch (DAOException e) {
            throw new ManagerException("No se pudo verificar el acceso a reportes del practicante.", e);
        }
    }

    private void validateHasAssignedProject(int practitionerId) throws ManagerException {
        try {
            boolean hasAssignedProject = postulationDAO.hasAssignedProject(practitionerId);
            if (!hasAssignedProject) {
                throw new ManagerException("No puedes crear o enviar reportes sin tener un proyecto asignado. Postula a un proyecto primero.");
            }
        } catch (DAOException e) {
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