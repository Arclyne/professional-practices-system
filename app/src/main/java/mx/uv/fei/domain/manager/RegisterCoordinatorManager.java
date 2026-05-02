package mx.uv.fei.domain.manager;

import java.util.UUID;

import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.repositories.CoordinatorDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerException;

public class RegisterCoordinatorManager {

    private UserDAO userDAO;
    private CoordinatorDAO coordinatorDAO;

    public RegisterCoordinatorManager(IDatabaseConnection dbConnection) {
        this.userDAO = new UserDAO(dbConnection);
        this.coordinatorDAO = new CoordinatorDAO(dbConnection, userDAO);
    }

    public String registerNewCoordinator(Coordinator coordinator) throws ManagerException {
        String tempPassword = this.generatePassword();
        coordinator.setPassword(tempPassword);
        coordinator.setStatus("no activo");

        try {
            int resultId = this.coordinatorDAO.insertCoordinator(coordinator);

            if (resultId <= 0) {
                throw new ManagerException("No se pudo completar el registro del coordinador en el sistema.");
            }

            return tempPassword;

        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.",
                    e);
        }
    }

    private String generatePassword() {
        return "temp-" + UUID.randomUUID().toString().substring(0, 8);
    }
}