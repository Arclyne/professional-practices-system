package mx.uv.fei.domain.manager;

import java.util.UUID;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class CoordinatorManager {

    private final ICoordinatorDAO coordinatorDataAccessObject;
    private final IUserDAO userDataAccessObject;

    @Inject
    public CoordinatorManager(ICoordinatorDAO coordinatorDataAccessObject, IUserDAO userDataAccessObject) {
        this.coordinatorDataAccessObject = coordinatorDataAccessObject;
        this.userDataAccessObject = userDataAccessObject;
    }

    public String registerNewCoordinator(Coordinator coordinatorInformation) throws ManagerException {
        String temporaryGeneratedPassword = this.generateTemporaryPassword();
        coordinatorInformation.setPassword(temporaryGeneratedPassword);
        coordinatorInformation.setStatus("Pendiente");

        try {
            int insertedCoordinatorId = this.coordinatorDataAccessObject.insertCoordinator(coordinatorInformation);

            if (insertedCoordinatorId <= 0) {
                throw new ManagerException("No se pudo completar el registro del coordinador en el sistema.");
            }

            return temporaryGeneratedPassword;

        } catch (DAOException dataAccessObjectException) {
            throw new ManagerException("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", dataAccessObjectException);
        }
    }

    public void inactivateCoordinator(int coordinatorIdentifier) throws ManagerException {
        try {
            boolean isCoordinatorDeactivated = userDataAccessObject.deactivateUser(coordinatorIdentifier);

            if (!isCoordinatorDeactivated) {
                throw new ManagerException("No se pudo inactivar. Verifique que el coordinador exista en el sistema.");
            }
        } catch (DAOException dataAccessObjectException) {
            throw new ManagerException("Error crítico de conexión al intentar cambiar el estado del coordinador.", dataAccessObjectException);
        }
    }

    public Coordinator retrieveCurrentCoordinator() throws ManagerException {
        try {
            return coordinatorDataAccessObject.getCurrentCoordinator();
        } catch (DAOException dataAccessObjectException) {
            throw new ManagerException("Error al consultar el coordinador en turno en el sistema.", dataAccessObjectException);
        }
    }

    private String generateTemporaryPassword() {
        return "temp-" + UUID.randomUUID().toString().substring(0, 8);
    }
}