package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IOrganizationDAO;
import mx.uv.fei.dataacces.interfaces.IProjectManagerDAO;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class OrganizationManager {

    private final IOrganizationDAO organizationDAO;
    private final IProjectManagerDAO projectManagerDAO;

    @Inject
    public OrganizationManager(IOrganizationDAO organizationDAO, IProjectManagerDAO projectManagerDAO) {
        this.organizationDAO = organizationDAO;
        this.projectManagerDAO = projectManagerDAO;
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
            return projectManagerDAO.getManagersByOrganization(organizationId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudieron cargar los encargados de esta organización.", e);
        }
    }
}