package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IAdministratorDAO;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.AuthenticatorAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AdminManager {

    private final IAdministratorDAO adminDAO;
    private final AppStore store;
    private static final Logger logger = LoggerFactory.getLogger(StartSessionManager.class);

    @Inject
    public AdminManager(IAdministratorDAO adminDAO, AppStore store) {
        this.adminDAO = adminDAO;
        this.store = store;
    }

    public boolean checkSystemHasAdmin() throws ManagerException {
        try {
            return this.adminDAO.checkIfAdminExists();
        } catch (DAOException exception) {
            throw new ManagerException("Error crítico al verificar el estado inicial del sistema.", exception);
        }
    }

    public void registerInitialAdmin(Administrator administrator) throws ManagerException {
        administrator.setStatus(UserStatus.ACTIVE);
        administrator.setRole("Administrator");

        UserValidator.validateAdministratorData(administrator);

        try {
            int resultId = this.adminDAO.insertAdministrator(administrator);

            if (resultId <= 0) {
                throw new ManagerException("No se pudo completar el registro del administrador en el sistema.");
            }
            store.dispatch(new AuthenticatorAction.AdminCreatedSuccessfully());

        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un problema de conexión con el servidor o los datos ya existen. Por favor, verifique la información.", e);
        }
    }
}