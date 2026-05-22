package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.exceptions.ManagerException;


@Component
public class ActivityManager {

    private static final String REGISTER_ACTIVITY_ERROR_MESSAGE = "No se pudo completar el registro de la actividad en el sistema.";
    private static final String CONNECTION_ERROR_MESSAGE = "Ocurrió un problema. Por favor, intente más tarde.";

    private final IActivityDAO activityDataAccessObject;

    @Inject
    public ActivityManager(IActivityDAO activityDataAccessObject) {
        this.activityDataAccessObject = activityDataAccessObject;
    }

    public boolean registerNewActivity(Activity activityToRegister) throws ManagerException {
        boolean isRegistered;

        Validator.validateActivityData(activityToRegister);

        try {
            isRegistered = activityDataAccessObject.insertActivity(activityToRegister);

            if (!isRegistered) {
                throw new ManagerException(REGISTER_ACTIVITY_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(CONNECTION_ERROR_MESSAGE, exception);
        }

        return isRegistered;
    }
}