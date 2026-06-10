package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CoordinatorManager {

    private static final Logger log = LoggerFactory.getLogger(CoordinatorManager.class);

    private final ICoordinatorDAO coordinatorDAO;
    private final IUserDAO userDAO;

    @Inject
    public CoordinatorManager(ICoordinatorDAO coordinatorDAO, IUserDAO userDAO) {
        this.coordinatorDAO = coordinatorDAO;
        this.userDAO = userDAO;
    }

    public String registerNewCoordinator(Coordinator coordinator) throws ManagerException {
        String temporaryPassword = PasswordManager.generatePassword();
        coordinator.setPassword(temporaryPassword);
        coordinator.setStatus(UserStatus.PENDING);
        coordinator.setRole("Coordinator");
        UserValidator.validateCoordinatorData(coordinator);

        try {
            int generatedId = coordinatorDAO.insertCoordinator(coordinator);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo completar el registro del coordinador en el sistema.");
            }
            return temporaryPassword;
        } catch (DAOException e) {
            log.error("Error al insertar el coordinador.", e);
            throw new ManagerException("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", e);
        }
    }

    public void inactivateCoordinator(int coordinatorId) throws ManagerException {
        try {
            boolean isDeactivated = userDAO.deactivateUser(coordinatorId);
            if (!isDeactivated) {
                throw new ManagerException("No se pudo inactivar. Verifique que el coordinador exista en el sistema.");
            }
        } catch (DAOException e) {
            log.error("Error al inactivar el coordinador con ID: {}.", coordinatorId, e);
            throw new ManagerException("Error crítico de conexión al intentar cambiar el estado del coordinador.", e);
        }
    }

    public Coordinator retrieveCurrentCoordinator() throws ManagerException {
        try {
            return coordinatorDAO.getCurrentCoordinator();
        } catch (DAOException e) {
            log.error("Error al recuperar el coordinador activo.", e);
            throw new ManagerException("Error al consultar el coordinador en turno en el sistema.", e);
        }
    }
}