package mx.uv.fei.domain.manager;

import java.util.UUID;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class CoordinatorManager {

    private final ICoordinatorDAO coordinatorDAO;

    @Inject
    public CoordinatorManager(ICoordinatorDAO coordinatorDAO) {
        this.coordinatorDAO = coordinatorDAO;
    }

    public void registerNewCoordinator(Coordinator coordinator) throws ManagerException {
        String tempPassword = this.generatePassword();
        coordinator.setPassword(tempPassword);
        Validator.validateCoordinatorData(coordinator);
        try {
            int resultId = this.coordinatorDAO.insertCoordinator(coordinator);

            if (resultId <= 0) {
                throw new ManagerException("No se pudo completar el registro del coordinador en el sistema.");
            }

        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", e);
        }
    }

    private String generatePassword() {
        return "temp-" + UUID.randomUUID().toString().substring(0, 8);
    }
}