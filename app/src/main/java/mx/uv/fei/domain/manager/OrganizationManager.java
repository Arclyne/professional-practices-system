package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IManagerDAO; // <-- Interfaz corregida
import mx.uv.fei.dataacces.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class OrganizationManager {

    private final IOrganizationDAO organizationDAO;
    private final IManagerDAO managerDAO;

    @Inject
    public OrganizationManager(IOrganizationDAO organizationDAO, IManagerDAO managerDAO) {
        this.organizationDAO = organizationDAO;
        this.managerDAO = managerDAO;
    }

    public List<Organization> getAllOrganizations() throws ManagerException {
        try {
            return organizationDAO.getAllOrganization();
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar la lista de organizaciones.", e);
        }
    }

    public List<Manager> getManagersByOrganization(int organizationId) throws ManagerException {
        try {
            return managerDAO.getManagersByOrganization(organizationId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudieron cargar los encargados de esta organización.", e);
        }
    }

    public boolean registerManager(Manager managerToRegister) throws ManagerException {
        Validator.validateManagerData(managerToRegister);

        try {
            boolean isRegistered = managerDAO.insertManager(managerToRegister);

            if (!isRegistered) {
                throw new ManagerException("No se pudo completar el registro del encargado en el sistema.");
            }
            return true;

        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    public void inactivateMultipleManagers(List<Integer> managerIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = managerDAO.deactivateMultipleManagers(managerIdentifiersList);
            if (!isProcessSuccessful) {
                throw new ManagerException("No se pudieron inactivar los encargados seleccionados.");
            }
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error de base de datos al inactivar encargados.", dataAccessException);
        }
    }

    public List<Manager> getAllManagers() throws ManagerException {
        try {
            return managerDAO.getAllManagers();
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error al obtener la lista de encargados.", dataAccessException);
        }
    }
}