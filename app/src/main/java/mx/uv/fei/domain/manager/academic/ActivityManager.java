package mx.uv.fei.domain.manager.academic;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.domain.common.validators.ReportValidator;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.enums.DocumentCategory;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class ActivityManager {

    private final IActivityDAO activityDAO;
    private final IPractitionerDocumentDAO documentDAO;

    @Inject
    public ActivityManager(IActivityDAO activityDAO, IPractitionerDocumentDAO documentDAO) {
        this.activityDAO = activityDAO;
        this.documentDAO = documentDAO;
    }

    public void registerActivity(Activity activity) throws ManagerException {
        validateAllInitialDocumentsAccepted(activity.getPractitionerId());
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

    private void validateAllInitialDocumentsAccepted(int practitionerId) throws ManagerException {
        try {
            boolean areInitialDocumentsAccepted = documentDAO.areAllDocumentsAccepted(
                    practitionerId, DocumentCategory.INITIAL.getDatabaseValue());
            if (!areInitialDocumentsAccepted) {
                throw new ManagerException("Aún no puedes registrar tareas. "
                        + "Tu profesor debe aceptar todos tus documentos iniciales primero.");
            }
        } catch (DAOException e) {
            throw new ManagerException("No se pudieron verificar los documentos iniciales del practicante.", e);
        }
    }
}
