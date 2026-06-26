package mx.uv.fei.domain.manager.people;
import mx.uv.fei.domain.manager.identity.PasswordManager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.common.PersistenceErrorTranslator;
import mx.uv.fei.domain.common.security.PasswordHasher;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        ensureNoActiveCoordinator();

        String temporaryPassword = PasswordManager.generatePassword();
        coordinator.setPassword(temporaryPassword);
        coordinator.setStatus(UserStatus.PENDING);
        coordinator.setRole("Coordinator");
        UserValidator.validateCoordinatorData(coordinator);
        coordinator.setPassword(PasswordHasher.hash(temporaryPassword));

        try {
            int generatedId = coordinatorDAO.insertCoordinator(coordinator);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo completar el registro del coordinador en el sistema.");
            }
            return temporaryPassword;
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw PersistenceErrorTranslator.translate(e);
        }
    }

    public void inactivateCoordinator(int coordinatorId) throws ManagerException {
        try {
            userDAO.deactivateUser(coordinatorId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error crítico de conexión al intentar cambiar el estado del coordinador.", e);
        }
    }

    public void activateCoordinator(int coordinatorId) throws ManagerException {
        ensureNoActiveCoordinator();

        try {
            userDAO.activateUser(coordinatorId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw PersistenceErrorTranslator.translate(e);
        }
    }

    public Coordinator retrieveCurrentCoordinator() throws ManagerException {
        try {
            return coordinatorDAO.getCurrentCoordinator();
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al consultar el coordinador en turno en el sistema.", e);
        }
    }

    public List<Coordinator> getAllCoordinators() throws ManagerException {
        try {
            return coordinatorDAO.getAllCoordinators();
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al obtener la lista de coordinadores.", e);
        }
    }

    public Coordinator getCoordinatorById(int coordinatorId) throws ManagerException {
        try {
            return coordinatorDAO.recoverCoordinator(coordinatorId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo recuperar la información del coordinador.", e);
        }
    }

    public void updateCoordinator(Coordinator coordinator, int coordinatorId) throws ManagerException {
        UserValidator.validateCoordinatorForUpdate(coordinator);

        try {
            coordinatorDAO.updateCoordinator(coordinator, coordinatorId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw PersistenceErrorTranslator.translate(e);
        }
    }

    private void ensureNoActiveCoordinator() throws ManagerException {
        Coordinator currentCoordinator = retrieveCurrentCoordinator();
        if (currentCoordinator != null) {
            throw new ManagerException(
                    "Ya existe un coordinador activo. Inactívalo antes de registrar o activar a otro.");
        }
    }

}