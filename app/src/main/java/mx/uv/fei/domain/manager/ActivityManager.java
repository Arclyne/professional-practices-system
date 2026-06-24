package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.common.validators.ReportValidator;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class ActivityManager {

    private final IActivityDAO activityDAO;
    private final IPractitionerDAO practitionerDAO;

    @Inject
    public ActivityManager(IActivityDAO activityDAO, IPractitionerDAO practitionerDAO) {
        this.activityDAO = activityDAO;
        this.practitionerDAO = practitionerDAO;
    }

    public void registerActivity(Activity activity) throws ManagerException {
        validateReportsAccessGranted(activity.getPractitionerId());
        ReportValidator.validateLogbookActivity(activity);
        try {
            int generatedId = activityDAO.insertActivity(activity);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo registrar la actividad en la bitácora.");
            }
        } catch (DAOException e) {
            throw new ManagerException("No se pudo registrar la actividad en la bitácora.", e);
        }
    }

    public void modifyActivity(Activity activity, int activityId) throws ManagerException {
        ReportValidator.validateLogbookActivity(activity);
        try {
            activityDAO.updateActivity(activity, activityId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo actualizar la información de la actividad.", e);
        }
    }

    public List<Activity> getPractitionerLogbook(int practitionerId) throws ManagerException {
        try {
            return activityDAO.getActivitiesByPractitioner(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al cargar las actividades del practicante.", e);
        }
    }

    public List<Activity> getActivitiesByReport(int reportId) throws ManagerException {
        try {
            return activityDAO.getActivitiesByReport(reportId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al cargar las actividades del reporte.", e);
        }
    }

    private void validateReportsAccessGranted(int practitionerId) throws ManagerException {
        try {
            boolean isAccessGranted = practitionerDAO.isReportsAccessGranted(practitionerId);
            if (!isAccessGranted) {
                throw new ManagerException("Tu profesor aún no habilita el registro de tareas. "
                        + "Debe aceptar todos tus documentos iniciales primero.");
            }
        } catch (DAOException e) {
            throw new ManagerException("No se pudo verificar el acceso a reportes del practicante.", e);
        }
    }
}
