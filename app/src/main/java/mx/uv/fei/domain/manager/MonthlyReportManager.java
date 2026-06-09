package mx.uv.fei.domain.manager;

import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IMonthlyReportDAO;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.domain.common.validators.ReportValidator;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class MonthlyReportManager {

    private static final String MSG_REGISTER_ERROR   = "No se pudo generar el reporte mensual.";
    private static final String MSG_RETRIEVE_ERROR   = "Ocurrió un error al cargar los reportes.";
    private static final String MSG_LINK_ERROR       = "El reporte se creó, pero hubo un problema al vincular las actividades seleccionadas.";
    private static final String MSG_NO_PROJECT       = "No puedes crear o enviar reportes sin tener un proyecto asignado. Postula a un proyecto primero.";
    private static final String MSG_PROJECT_CHECK_ERROR = "No se pudo verificar el estado de tu proyecto asignado.";

    private final IMonthlyReportDAO reportDAO;
    private final IActivityDAO activityDAO;
    private final IPostulationDAO postulationDAO;

    @Inject
    public MonthlyReportManager(IMonthlyReportDAO reportDAO, IActivityDAO activityDAO, IPostulationDAO postulationDAO) {
        this.reportDAO = reportDAO;
        this.activityDAO = activityDAO;
        this.postulationDAO = postulationDAO;
    }

    public void createReportAndLinkActivities(MonthlyReport report, List<Activity> selectedActivities) throws ManagerException {
        validateHasAssignedProject(report.getPractitionerId());
        ReportValidator.validateMonthlyReportCreation(report, selectedActivities);

        try {
            int reportId = reportDAO.insertReport(report);
            if (reportId <= 0) {
                throw new ManagerException(MSG_REGISTER_ERROR);
            }

            if (selectedActivities != null && !selectedActivities.isEmpty()) {
                for (Activity activity : selectedActivities) {
                    boolean isLinked = activityDAO.assignActivityToReport(activity.getActivityId(), reportId);
                    if (!isLinked) {
                        throw new ManagerException(MSG_LINK_ERROR);
                    }
                }
            }
        } catch (DAOException e) {
            throw new ManagerException(MSG_REGISTER_ERROR + " Causa: " + e.getMessage(), e);
        }
    }

    public List<MonthlyReport> getPractitionerReports(int practitionerId) throws ManagerException {
        List<MonthlyReport> reports;

        try {
            reports = reportDAO.getReportsByPractitioner(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, e);
        }

        return reports;
    }

    public void submitSignedReport(MonthlyReport report, String signedFileUrl) throws ManagerException {
        validateHasAssignedProject(report.getPractitionerId());
        ReportValidator.validateSignedReport(signedFileUrl);

        report.setSignedFileUrl(signedFileUrl);
        report.setStatus(ReportStatus.SUBMITTED.getDatabaseValue());

        try {
            boolean isUpdated = reportDAO.updateReport(report, report.getReportId());
            if (!isUpdated) {
                throw new ManagerException("No se pudo actualizar el estado del reporte en la base de datos.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Error al enviar el reporte firmado. Causa: " + e.getMessage(), e);
        }
    }

    public List<MonthlyReport> getReportsForEvaluation() throws ManagerException {
        List<MonthlyReport> reports;

        try {
            reports = reportDAO.getSubmittedReports();
        } catch (DAOException e) {
            throw new ManagerException("Error al cargar los reportes para evaluar.", e);
        }

        return reports;
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

            boolean isUpdated = reportDAO.updateReport(report, reportId);
            if (!isUpdated) {
                throw new ManagerException("No se pudo guardar la evaluación en la base de datos.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al intentar guardar la evaluación. Causa: " + e.getMessage(), e);
        }
    }

    public boolean verifyHasAssignedProject(int practitionerId) throws ManagerException {
        try {
            return postulationDAO.hasAssignedProject(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException(MSG_PROJECT_CHECK_ERROR, e);
        }
    }

    private void validateHasAssignedProject(int practitionerId) throws ManagerException {
        try {
            boolean hasProject = postulationDAO.hasAssignedProject(practitionerId);
            if (!hasProject) {
                throw new ManagerException(MSG_NO_PROJECT);
            }
        } catch (DAOException e) {
            throw new ManagerException(MSG_PROJECT_CHECK_ERROR, e);
        }
    }
}
