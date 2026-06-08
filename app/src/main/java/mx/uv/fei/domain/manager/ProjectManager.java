package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.common.validators.ProjectValidator;
import mx.uv.fei.domain.exceptions.ManagerException;
import java.util.List;

@Component
public class ProjectManager {

    private final IProjectDAO projectDAO;

    @Inject
    public ProjectManager(IProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public void registerNewProject(Project projectToRegister) throws ManagerException {
        ProjectValidator.validateProjectData(projectToRegister);

        try {
            boolean isRegistered = projectDAO.insertProject(projectToRegister);

            if (!isRegistered) {
                throw new ManagerException("No se pudo completar el registro del proyecto en el sistema.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema. Por favor, intente más tarde.", e);
        }
    }

    public void inactivateMultipleProjects(List<Integer> projectIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = projectDAO.deactivateMultipleProjects(projectIdentifiersList);
            if (!isProcessSuccessful) {
                throw new ManagerException("No se pudieron inactivar los proyectos seleccionados.");
            }
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error de base de datos al inactivar proyectos.", dataAccessException);
        }
    }

    public List<Project> getAllProjects() throws ManagerException {
        try {
            return projectDAO.getAllProjects();
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error al obtener la lista de proyectos.", dataAccessException);
        }
    }

}