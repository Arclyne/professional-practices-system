package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.domain.common.validators.ProjectValidator;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class ProjectManager {

    private final IProjectDAO projectDAO;

    @Inject
    public ProjectManager(IProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public void registerNewProject(Project project) throws ManagerException {
        ProjectValidator.validateProjectData(project);
        try {
            boolean isRegistered = projectDAO.insertProject(project);
            if (!isRegistered) {
                throw new ManagerException("No se pudo completar el registro del proyecto en el sistema.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    public void inactivateMultipleProjects(List<Integer> projectIds) throws ManagerException {
        try {
            boolean isDeactivationSuccessful = projectDAO.deactivateMultipleProjects(projectIds);
            if (!isDeactivationSuccessful) {
                throw new ManagerException("No se pudieron inactivar los proyectos seleccionados.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Error de base de datos al inactivar proyectos.", e);
        }
    }

    public List<Project> getAllProjects() throws ManagerException {
        try {
            return projectDAO.getAllProjects();
        } catch (DAOException e) {
            throw new ManagerException("Error al obtener la lista de proyectos.", e);
        }
    }
}