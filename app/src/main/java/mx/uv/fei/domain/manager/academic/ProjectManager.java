package mx.uv.fei.domain.manager.academic;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.domain.common.validators.ProjectValidator;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Component
public class ProjectManager {

    private static final Logger log = LoggerFactory.getLogger(ProjectManager.class);
    private final IProjectDAO projectDAO;

    @Inject
    public ProjectManager(IProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public void registerNewProject(Project project) throws ManagerException {
        ProjectValidator.validateProjectData(project);
        try {
            projectDAO.insertProject(project);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    public void updateProject(Project project, int id) throws ManagerException {
        ProjectValidator.validateProjectData(project);
        try {
            projectDAO.updateProject(project, id);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al actualizar el proyecto en la base de datos.", e);
        }
    }

    public void inactivateMultipleProjects(List<Integer> projectIds) throws ManagerException {
        try {
            projectDAO.deactivateMultipleProjects(projectIds);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error de base de datos al inactivar proyectos.", e);
        }
    }

    public void inactivateProject(int projectId) throws ManagerException {
        inactivateMultipleProjects(List.of(projectId));
    }

    public void activateProject(int projectId) throws ManagerException {
        try {
            projectDAO.activateProject(projectId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error de base de datos al activar el proyecto.", e);
        }
    }

    public List<Project> getAllProjects() throws ManagerException {
        try {
            return projectDAO.getAllProjects();
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al obtener la lista de proyectos.", e);
        }
    }

    public Map<Integer, Integer> getAssignedCountsByProject() throws ManagerException {
        try {
            return projectDAO.getAssignedCountsByProject();
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al obtener los cupos ocupados de los proyectos.", e);
        }
    }
}