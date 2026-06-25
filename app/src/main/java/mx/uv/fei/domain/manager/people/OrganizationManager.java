package mx.uv.fei.domain.manager.people;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.common.PersistenceErrorTranslator;
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
            organizationDAO.insertOrganization(organization);
        } catch (DAOException e) {
            throw PersistenceErrorTranslator.translate(e);
        }
    }

    public List<Organization> getAllOrganizations() throws ManagerException {
        try {
            return organizationDAO.getAllOrganizations();
        } catch (DAOException e) {
            throw new ManagerException("Error al recuperar la lista de organizaciones.", e);
        }
    }

    public void updateOrganization(Organization organization, int id) throws ManagerException {
        OrganizationValidator.validateOrganizationData(organization);

        try {
            organizationDAO.updateOrganization(organization, id);
        } catch (DAOException e) {
            throw PersistenceErrorTranslator.translate(e);
        }
    }

    public void inactivateMultipleOrganizations(List<Integer> organizationIds) throws ManagerException {
        try {
            organizationDAO.deactivateMultipleOrganizations(organizationIds);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo inactivar la organización. Intenta de nuevo en unos momentos.", e);
        }
    }

    public void inactivateOrganization(int organizationId) throws ManagerException {
        inactivateMultipleOrganizations(List.of(organizationId));
    }

    public void activateOrganization(int organizationId) throws ManagerException {
        try {
            organizationDAO.activateOrganization(organizationId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo activar la organización. Intenta de nuevo en unos momentos.", e);
        }
    }
}