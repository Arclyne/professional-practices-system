package mx.uv.fei.domain.manager.people;

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
            managerDAO.insertManager(manager);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    public void inactivateMultipleManagers(List<Integer> managerIds) throws ManagerException {
        try {
            managerDAO.deactivateMultipleManagers(managerIds);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo inactivar el encargado. Intenta de nuevo en unos momentos.", e);
        }
    }

    public void inactivateManager(int managerId) throws ManagerException {
        inactivateMultipleManagers(List.of(managerId));
    }

    public void activateManager(int managerId) throws ManagerException {
        try {
            managerDAO.activateManager(managerId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo activar el encargado. Intenta de nuevo en unos momentos.", e);
        }
    }

    public Manager getManagerById(int managerId) throws ManagerException {
        try {
            return managerDAO.recoverManager(managerId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo recuperar la información del encargado.", e);
        }
    }

    public void updateManager(Manager manager, int managerId) throws ManagerException {
        UserValidator.validateManagerData(manager);

        try {
            managerDAO.updateManager(manager, managerId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }
}