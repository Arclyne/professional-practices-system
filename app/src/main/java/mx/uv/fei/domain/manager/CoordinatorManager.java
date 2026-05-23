package mx.uv.fei.domain.manager;

import java.util.UUID;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class CoordinatorManager {

    private static final Logger logger = LoggerFactory.getLogger(CoordinatorManager.class);
    private final ICoordinatorDAO coordinatorDataAccessObject;
    private final IUserDAO userDAO;

    @Inject
    public CoordinatorManager(ICoordinatorDAO coordinatorDAO, IUserDAO userDataAccessObject) {
        this.coordinatorDataAccessObject = coordinatorDAO;
        this.userDAO = userDataAccessObject;
    }

    public String registerNewCoordinator(Coordinator coordinatorInformation) throws ManagerException {
        String temporaryGeneratedPassword = PasswordManager.generatePassword();
        coordinatorInformation.setPassword(temporaryGeneratedPassword);
        coordinatorInformation.setStatus(UserStatus.PENDING);

        Validator.validateCoordinatorData(coordinatorInformation);

        try {
            int insertedCoordinatorId = this.coordinatorDataAccessObject.insertCoordinator(coordinatorInformation);

            if (insertedCoordinatorId <= 0) {
                throw new ManagerException("No se pudo completar el registro del coordinador en el sistema.");
            }

            return temporaryGeneratedPassword;

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
            return coordinatorDataAccessObject.getCurrentCoordinator();
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            throw new ManagerException("Error al consultar el coordinador en turno en el sistema.", e);
        }
    }
}