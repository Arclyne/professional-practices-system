package mx.uv.fei.domain.manager;

import java.util.List;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IManagerDAO;
import mx.uv.fei.domain.common.Validator;
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

    private final IManagerDAO managerDataAccessObject;

    @Inject
    public ManagerManager(IManagerDAO managerDataAccessObject) {
        this.managerDataAccessObject = managerDataAccessObject;
    }

    public List<Manager> getAllManagers() throws ManagerException {
        List<Manager> managers;

        try {
            managers = managerDataAccessObject.getAllManagers();
        } catch (DAOException exception) {
            throw new ManagerException(GET_ALL_ERROR_MESSAGE, exception);
        }

        return managers;
    }

    public List<Manager> getManagersByOrganization(int organizationId) throws ManagerException {
        List<Manager> managers;

        try {
            managers = managerDataAccessObject.getManagersByOrganization(organizationId);
        } catch (DAOException exception) {
            throw new ManagerException(GET_MANAGERS_ERROR_MESSAGE, exception);
        }

        return managers;
    }

    public boolean registerManager(Manager managerToRegister) throws ManagerException {
        boolean isRegistered;

        Validator.validateManagerData(managerToRegister);

        try {
            isRegistered = managerDataAccessObject.insertManager(managerToRegister);

            if (!isRegistered) {
                throw new ManagerException(REGISTER_MANAGER_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(CONNECTION_ERROR_MESSAGE, exception);
        }

        return isRegistered;
    }

    public void inactivateMultipleManagers(List<Integer> managerIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = managerDataAccessObject.deactivateMultipleManagers(managerIdentifiersList);

            if (!isProcessSuccessful) {
                throw new ManagerException(INACTIVATE_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(INACTIVATE_CONNECTION_ERROR_MESSAGE, exception);
        }
    }
}