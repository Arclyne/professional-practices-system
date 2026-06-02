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

    private static final String MSG_REGISTER_ERROR = "The activity could not be registered in the system.";
    private static final String MSG_CONNECTION_ERROR = "A connection problem occurred. Please try again later.";

    private final IActivityDAO activityDAO;

    @Inject
    public ActivityManager(IActivityDAO activityDAO) {
        this.activityDAO = activityDAO;
    }

    public void registerNewActivity(Activity activityToRegister) throws ManagerException {
        Validator.validateActivityData(activityToRegister);

        try {
            boolean isRegistered = activityDAO.insertActivity(activityToRegister);

            if (!isRegistered) {
                throw new ManagerException(MSG_REGISTER_ERROR);
            }
        } catch (DAOException exception) {
            throw new ManagerException(MSG_CONNECTION_ERROR, exception);
        }
    }
}