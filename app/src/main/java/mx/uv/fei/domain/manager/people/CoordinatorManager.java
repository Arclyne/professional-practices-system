package mx.uv.fei.domain.manager.people;
import mx.uv.fei.domain.manager.identity.PasswordManager;

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

import java.sql.SQLIntegrityConstraintViolationException;
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

        try {
            int generatedId = coordinatorDAO.insertCoordinator(coordinator);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo completar el registro del coordinador en el sistema.");
            }
            return temporaryPassword;
        } catch (DAOException e) {
            log.error("Error al insertar el coordinador.", e);
            throw translatePersistenceFailure(e);
        }
    }

    public void inactivateCoordinator(int coordinatorId) throws ManagerException {
        try {
            userDAO.deactivateUser(coordinatorId);
        } catch (DAOException e) {
            log.error("Error al inactivar el coordinador con ID: {}.", coordinatorId, e);
            throw new ManagerException("Error crítico de conexión al intentar cambiar el estado del coordinador.", e);
        }
    }

    public void activateCoordinator(int coordinatorId) throws ManagerException {
        ensureNoActiveCoordinator();

        try {
            userDAO.activateUser(coordinatorId);
        } catch (DAOException e) {
            log.error("Error al activar el coordinador con ID: {}.", coordinatorId, e);
            throw new ManagerException("Error crítico de conexión al intentar activar el coordinador.", e);
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

    public List<Coordinator> getAllCoordinators() throws ManagerException {
        try {
            return coordinatorDAO.getAllCoordinators();
        } catch (DAOException e) {
            throw new ManagerException("Error al obtener la lista de coordinadores.", e);
        }
    }

    public Coordinator getCoordinatorById(int coordinatorId) throws ManagerException {
        try {
            return coordinatorDAO.recoverCoordinator(coordinatorId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo recuperar la información del coordinador.", e);
        }
    }

    public void updateCoordinator(Coordinator coordinator, int coordinatorId) throws ManagerException {
        UserValidator.validateCoordinatorForUpdate(coordinator);

        try {
            coordinatorDAO.updateCoordinator(coordinator, coordinatorId);
        } catch (DAOException e) {
            log.error("Error al actualizar el coordinador.", e);
            throw translatePersistenceFailure(e);
        }
    }

    private void ensureNoActiveCoordinator() throws ManagerException {
        Coordinator currentCoordinator = retrieveCurrentCoordinator();
        if (currentCoordinator != null) {
            throw new ManagerException(
                    "Ya existe un coordinador activo. Inactívalo antes de registrar o activar a otro.");
        }
    }

    private ManagerException translatePersistenceFailure(DAOException e) {
        String friendlyMessage = "Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.";
        SQLIntegrityConstraintViolationException duplicateViolation = findIntegrityViolation(e);

        if (duplicateViolation != null) {
            friendlyMessage = describeDuplicate(duplicateViolation);
        }

        return new ManagerException(friendlyMessage, e);
    }

    private SQLIntegrityConstraintViolationException findIntegrityViolation(Throwable error) {
        SQLIntegrityConstraintViolationException violation = null;
        Throwable cause = error;

        while (cause != null && violation == null) {
            if (cause instanceof SQLIntegrityConstraintViolationException integrityViolation) {
                violation = integrityViolation;
            }
            cause = cause.getCause();
        }

        return violation;
    }

    private String describeDuplicate(SQLIntegrityConstraintViolationException violation) {
        String message = "Ya existe un registro con los datos proporcionados.";
        String detail = violation.getMessage() != null ? violation.getMessage().toLowerCase() : "";

        if (detail.contains("email")) {
            message = "Este correo ya está registrado en otra cuenta.";
        } else if (detail.contains("username")) {
            message = "Este número de personal o usuario ya está en uso.";
        }

        return message;
    }
}