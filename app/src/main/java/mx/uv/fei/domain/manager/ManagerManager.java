package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IManagerDAO;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class ManagerManager {

    private final IManagerDAO managerDAO;

    @Inject
    public ManagerManager(IManagerDAO managerDAO) {
        this.managerDAO = managerDAO;
    }

    public List<Manager> getAllManagers() throws ManagerException {
        try {
            return managerDAO.getAllManagers();
        } catch (DAOException e) {
            throw new ManagerException("Error al obtener la lista de encargados.", e);
        }
    }

    public List<Manager> getManagersByOrganization(int organizationId) throws ManagerException {
        try {
            return managerDAO.getManagersByOrganization(organizationId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudieron cargar los encargados de esta organización.", e);
        }
    }

    public void registerManager(Manager manager) throws ManagerException {
        UserValidator.validateManagerData(manager);
        try {
            boolean isRegistered = managerDAO.insertManager(manager);
            if (!isRegistered) {
                throw new ManagerException("No se pudo completar el registro del encargado en el sistema.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    public void inactivateMultipleManagers(List<Integer> managerIds) throws ManagerException {
        try {
            boolean isDeactivationSuccessful = managerDAO.deactivateMultipleManagers(managerIds);
            if (!isDeactivationSuccessful) {
                throw new ManagerException("No se pudieron inactivar los encargados seleccionados.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Error de base de datos al inactivar encargados.", e);
        }
    }
}