package mx.uv.fei.domain.manager;

import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IMonthlyReportDAO;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class MonthlyReportManager {

    private static final String MSG_REGISTER_ERROR = "No se pudo generar el reporte mensual.";
    private static final String MSG_RETRIEVE_ERROR = "Ocurrió un error al cargar los reportes.";
    private static final String MSG_LINK_ERROR = "El reporte se creó, pero hubo un problema al vincular las actividades seleccionadas.";

    private final IMonthlyReportDAO reportDAO;
    private final IActivityDAO activityDAO;

    @Inject
    public MonthlyReportManager(IMonthlyReportDAO reportDAO, IActivityDAO activityDAO) {
        this.reportDAO = reportDAO;
        this.activityDAO = activityDAO;
    }

    public void createReportAndLinkActivities(MonthlyReport report, List<Activity> selectedActivities) throws ManagerException {
        Validator.validateMonthlyReportCreation(report, selectedActivities);

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
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, exception);
        }
        return reports;
    }

    public void submitSignedReport(MonthlyReport report, String signedFileUrl) throws ManagerException {
        Validator.validateSignedReport(signedFileUrl);

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
        try {
            return reportDAO.getSubmittedReports();
        } catch (DAOException exception) {
            throw new ManagerException("Error al cargar los reportes para evaluar.", exception);
        }
    }

    public void evaluateReport(int reportId, Double grade, String feedback) throws ManagerException {
        Validator.validateReportEvaluation(grade, feedback);

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
}