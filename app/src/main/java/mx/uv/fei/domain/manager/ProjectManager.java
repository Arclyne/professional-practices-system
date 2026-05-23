package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.dataaccess.interfaces.IActivityDAO;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.exceptions.ManagerException;
import java.util.List;

@Component
public class ProjectManager {

    private final IActivityDAO activityDataAccessObject;
    private final IProjectDAO projectDataAccessObject;

    @Inject
    public ProjectManager(IActivityDAO activityDataAccessObject, IProjectDAO projectDataAccessObject) {
        this.activityDataAccessObject = activityDataAccessObject;
        this.projectDataAccessObject = projectDataAccessObject;
    }

    public boolean registerNewProject(Project projectToRegister) throws ManagerException {
        Validator.validateProjectData(projectToRegister);

        try {
            boolean isRegistered = projectDataAccessObject.insertProject(projectToRegister);

            if (!isRegistered) {
                throw new ManagerException("No se pudo completar el registro del proyecto en el sistema.");
            }
            return true;

        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema. Por favor, intente más tarde.", e);
        }
    }

    public void inactivateMultipleProjects(List<Integer> projectIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = projectDataAccessObject.deactivateMultipleProjects(projectIdentifiersList);
            if (!isProcessSuccessful) {
                throw new ManagerException("No se pudieron inactivar los proyectos seleccionados.");
            }
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error de base de datos al inactivar proyectos.", dataAccessException);
        }
    }

    public List<Project> getAllProjects() throws ManagerException {
        try {
            return projectDataAccessObject.getAllProjects();
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error al obtener la lista de proyectos.", dataAccessException);
        }
    }

}