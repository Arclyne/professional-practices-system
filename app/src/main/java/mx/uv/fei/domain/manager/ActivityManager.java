package mx.uv.fei.domain.manager;

import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.common.validators.ReportValidator;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class ActivityManager {

    private static final String MSG_REGISTER_ERROR = "No se pudo registrar la actividad en la bitácora.";
    private static final String MSG_UPDATE_ERROR = "No se pudo actualizar la información de la actividad.";
    private static final String MSG_RETRIEVE_ERROR = "Ocurrió un error al cargar tus actividades.";
    private static final String MSG_RETRIEVE_REPORT_ACT_ERROR = "Ocurrió un error al cargar las actividades del reporte.";

    private final IActivityDAO activityDAO;

    @Inject
    public ActivityManager(IActivityDAO activityDAO) {
        this.activityDAO = activityDAO;
    }

    public void registerActivity(Activity activity) throws ManagerException {
        ReportValidator.validateLogbookActivity(activity);

        try {
            int resultId = activityDAO.insertActivity(activity);
            if (resultId <= 0) {
                throw new ManagerException(MSG_REGISTER_ERROR);
            }
        } catch (DAOException exception) {
            throw new ManagerException(MSG_REGISTER_ERROR + " Causa: " + exception.getMessage(), exception);
        }
    }

    public void modifyActivity(Activity activity, int activityId) throws ManagerException {
        ReportValidator.validateLogbookActivity(activity);

        try {
            boolean isUpdated = activityDAO.updateActivity(activity, activityId);
            if (!isUpdated) {
                throw new ManagerException(MSG_UPDATE_ERROR);
            }
        } catch (DAOException exception) {
            throw new ManagerException(MSG_UPDATE_ERROR + " Causa: " + exception.getMessage(), exception);
        }
    }

    public List<Activity> getPractitionerLogbook(int practitionerId) throws ManagerException {
        List<Activity> activities;
        try {
            activities = activityDAO.getActivitiesByPractitioner(practitionerId);
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, exception);
        }
        return activities;
    }

    public List<Activity> getActivitiesByReport(int reportId) throws ManagerException {
        List<Activity> activities;
        try {
            activities = activityDAO.getActivitiesByReport(reportId);
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_REPORT_ACT_ERROR, exception);
        }
        return activities;
    }
}