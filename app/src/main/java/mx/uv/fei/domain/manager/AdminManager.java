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

    private static final Logger log = LoggerFactory.getLogger(AdminManager.class);

    private final IAdministratorDAO administratorDAO;
    private final AppStore store;

    @Inject
    public AdminManager(IAdministratorDAO administratorDAO, AppStore store) {
        this.administratorDAO = administratorDAO;
        this.store = store;
    }

    public boolean checkSystemHasAdmin() throws ManagerException {
        try {
            return administratorDAO.checkIfAdminExists();
        } catch (DAOException e) {
            throw new ManagerException("Error crítico al verificar el estado inicial del sistema.", e);
        }
    }

    public void registerInitialAdmin(Administrator administrator) throws ManagerException {
        administrator.setStatus(UserStatus.ACTIVE);
        administrator.setRole("Administrator");
        UserValidator.validateAdministratorData(administrator);

        try {
            int generatedId = administratorDAO.insertAdministrator(administrator);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo completar el registro del administrador en el sistema.");
            }
            store.dispatch(new AuthenticatorAction.AdminCreatedSuccessfully());
        } catch (DAOException e) {
            log.error("Error al insertar el administrador inicial.", e);
            throw new ManagerException("Ocurrió un problema de conexión con el servidor o los datos ya existen. Por favor, verifique la información.", e);
        }
    }
}