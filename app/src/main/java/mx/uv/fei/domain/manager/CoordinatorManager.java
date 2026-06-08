package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CoordinatorManager {

    private static final Logger logger = LoggerFactory.getLogger(CoordinatorManager.class);
    private final ICoordinatorDAO coordinatorDAO;
    private final IUserDAO userDAO;

    @Inject
    public CoordinatorManager(ICoordinatorDAO coordinatorDAO, IUserDAO userDAO) {
        this.coordinatorDAO = coordinatorDAO;
        this.userDAO = userDAO;
    }

    public String registerNewCoordinator(Coordinator coordinatorInformation) throws ManagerException {
        String temporaryPassword = PasswordManager.generatePassword();
        coordinatorInformation.setPassword(temporaryPassword);
        coordinatorInformation.setStatus(UserStatus.PENDING);
        coordinatorInformation.setRole("Coordinator");

        UserValidator.validateCoordinatorData(coordinatorInformation);

        try {
            int insertedCoordinatorId = this.coordinatorDAO.insertCoordinator(coordinatorInformation);

            if (insertedCoordinatorId <= 0) {
                throw new ManagerException("No se pudo completar el registro del coordinador en el sistema.");
            }

            return temporaryPassword;

        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", e);
        }
    }

    public void inactivateCoordinator(int coordinatorIdentifier) throws ManagerException {
        try {
            boolean isCoordinatorDeactivated = userDAO.deactivateUser(coordinatorIdentifier);

            if (!isCoordinatorDeactivated) {
                throw new ManagerException("No se pudo inactivar. Verifique que el coordinador exista en el sistema.");
            }
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            throw new ManagerException("Error crítico de conexión al intentar cambiar el estado del coordinador.", e);
        }
    }

    public Coordinator retrieveCurrentCoordinator() throws ManagerException {
        try {
            return coordinatorDAO.getCurrentCoordinator();
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            throw new ManagerException("Error al consultar el coordinador en turno en el sistema.", e);
        }
    }
}