package mx.uv.fei.domain.manager;

import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IManagerDAO;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class ManagerManager {

    private static final String GET_ALL_ERROR_MESSAGE = "Error al obtener la lista de encargados.";
    private static final String INACTIVATE_ERROR_MESSAGE = "No se pudieron inactivar los encargados seleccionados.";
    private static final String INACTIVATE_CONNECTION_ERROR_MESSAGE = "Error de base de datos al inactivar encargados.";
    private static final String GET_MANAGERS_ERROR_MESSAGE = "No se pudieron cargar los encargados de esta organización.";
    private static final String REGISTER_MANAGER_ERROR_MESSAGE = "No se pudo completar el registro del encargado en el sistema.";
    private static final String CONNECTION_ERROR_MESSAGE = "Ocurrió un problema de conexión. Por favor, intente más tarde.";

    private final IManagerDAO managerDAO;

    @Inject
    public ManagerManager(IManagerDAO managerDAO) {
        this.managerDAO = managerDAO;
    }

    public List<Manager> getAllManagers() throws ManagerException {
        List<Manager> managers;

        try {
            managers = managerDAO.getAllManagers();
        } catch (DAOException e) {
            throw new ManagerException(GET_ALL_ERROR_MESSAGE, e);
        }

        return managers;
    }

    public List<Manager> getManagersByOrganization(int organizationId) throws ManagerException {
        List<Manager> managers;

        try {
            managers = managerDAO.getManagersByOrganization(organizationId);
        } catch (DAOException e) {
            throw new ManagerException(GET_MANAGERS_ERROR_MESSAGE, e);
        }

        return managers;
    }

    public void registerManager(Manager managerToRegister) throws ManagerException {
        UserValidator.validateManagerData(managerToRegister);

        try {
            boolean isRegistered = managerDAO.insertManager(managerToRegister);

            if (!isRegistered) {
                throw new ManagerException(REGISTER_MANAGER_ERROR_MESSAGE);
            }
        } catch (DAOException e) {
            throw new ManagerException(CONNECTION_ERROR_MESSAGE, e);
        }
    }

    public void inactivateMultipleManagers(List<Integer> managerIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = managerDAO.deactivateMultipleManagers(managerIdentifiersList);

            if (!isProcessSuccessful) {
                throw new ManagerException(INACTIVATE_ERROR_MESSAGE);
            }
        } catch (DAOException e) {
            throw new ManagerException(INACTIVATE_CONNECTION_ERROR_MESSAGE, e);
        }
    }
}
