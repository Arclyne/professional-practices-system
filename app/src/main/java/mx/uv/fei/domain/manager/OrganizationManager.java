package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.common.validators.OrganizationValidator;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class OrganizationManager {

    private final IOrganizationDAO organizationDAO;

    @Inject
    public OrganizationManager(IOrganizationDAO organizationDAO) {
        this.organizationDAO = organizationDAO;
    }

    public void registerOrganization(Organization organization) throws ManagerException {
        OrganizationValidator.validateOrganizationData(organization);

        try {
            boolean isRegistered = organizationDAO.insertOrganization(organization);
            if (!isRegistered) {
                throw new ManagerException("No se pudo completar el registro de la organización en el sistema.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    public List<Organization> getAllOrganizations() throws ManagerException {
        try {
            return organizationDAO.getAllOrganizations();
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar la lista de organizaciones.", e);
        }
    }

    public void inactivateMultipleOrganizations(List<Integer> organizationIds) throws ManagerException {
        try {
            organizationDAO.deactivateMultipleOrganizations(organizationIds);
        } catch (DAOException e) {
            throw new ManagerException("Error de base de datos al inactivar organizaciones.", e);
        }
    }
}