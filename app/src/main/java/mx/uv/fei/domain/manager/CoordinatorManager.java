package mx.uv.fei.domain.manager;

import java.util.UUID;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.repositories.CoordinatorDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class CoordinatorManager {

    private final ICoordinatorDAO coordinatorDataAccessObject;

    @Inject
    public CoordinatorManager(ICoordinatorDAO coordinatorDataAccessObject) {
        this.coordinatorDataAccessObject = coordinatorDataAccessObject;
    }

    public String registerNewCoordinator(Coordinator coordinatorInformation) throws ManagerException {
        String temporaryGeneratedPassword = this.generateTemporaryPassword();
        coordinatorInformation.setPassword(temporaryGeneratedPassword);

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

    private String generateTemporaryPassword() {
        return "temp-" + UUID.randomUUID().toString().substring(0, 8);
    }
}