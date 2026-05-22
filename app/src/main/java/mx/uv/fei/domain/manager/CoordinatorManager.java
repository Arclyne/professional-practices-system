package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;


@Component
public class CoordinatorManager {

    private static final String REGISTER_ERROR_MESSAGE = "No se pudo completar el registro del coordinador en el sistema.";
    private static final String CONNECTION_ERROR_MESSAGE = "Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.";
    private static final String INACTIVATE_ERROR_MESSAGE = "No se pudo inactivar. Verifique que el coordinador exista en el sistema.";
    private static final String INACTIVATE_CONNECTION_ERROR_MESSAGE = "Error crítico de conexión al intentar cambiar el estado del coordinador.";
    private static final String RETRIEVE_ERROR_MESSAGE = "Error al consultar el coordinador en turno en el sistema.";
    private static final int MINIMUM_VALID_ID = 1;

    private final ICoordinatorDAO coordinatorDAO;
    private final IUserDAO userDAO;

    @Inject
    public CoordinatorManager(ICoordinatorDAO coordinatorDAO, IUserDAO userDataAccessObject) {
        this.coordinatorDAO = coordinatorDAO;
        this.userDAO = userDataAccessObject;
    }

    public String registerNewCoordinator(Coordinator coordinatorInformation) throws ManagerException {
        String temporaryGeneratedPassword = PasswordManager.generateTemporaryPassword();
        coordinatorInformation.setPassword(temporaryGeneratedPassword);
        coordinatorInformation.setStatus(UserStatus.PENDING);

        Validator.validateCoordinatorData(coordinatorInformation);

        try {
            int insertedCoordinatorId = coordinatorDAO.insertCoordinator(coordinatorInformation);

            if (insertedCoordinatorId < MINIMUM_VALID_ID) {
                throw new ManagerException(REGISTER_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(CONNECTION_ERROR_MESSAGE, exception);
        }

        return temporaryGeneratedPassword;
    }

    public void inactivateCoordinator(int coordinatorIdentifier) throws ManagerException {
        try {
            boolean isCoordinatorDeactivated = userDAO.deactivateUser(coordinatorIdentifier);

            if (!isCoordinatorDeactivated) {
                throw new ManagerException(INACTIVATE_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(INACTIVATE_CONNECTION_ERROR_MESSAGE, exception);
        }
    }

    public Coordinator retrieveCurrentCoordinator() throws ManagerException {
        Coordinator currentCoordinator;

        try {
            currentCoordinator = coordinatorDAO.getCurrentCoordinator();
        } catch (DAOException exception) {
            throw new ManagerException(RETRIEVE_ERROR_MESSAGE, exception);
        }

        return currentCoordinator;
    }
}