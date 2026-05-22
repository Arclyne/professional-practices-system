package mx.uv.fei.domain.manager;

import java.util.List;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.exceptions.ManagerException;


@Component
public class ProjectManager {

    private static final String REGISTER_PROJECT_ERROR_MESSAGE = "No se pudo completar el registro del proyecto en el sistema.";
    private static final String CONNECTION_ERROR_MESSAGE = "Ocurrió un problema. Por favor, intente más tarde.";
    private static final String INACTIVATE_PROJECTS_ERROR_MESSAGE = "No se pudieron inactivar los proyectos seleccionados.";
    private static final String INACTIVATE_CONNECTION_ERROR_MESSAGE = "Error de base de datos al inactivar proyectos.";
    private static final String GET_ALL_PROJECTS_ERROR_MESSAGE = "Error al obtener la lista de proyectos.";

    private final IProjectDAO projectDAO;

    @Inject
    public ProjectManager(IProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public boolean registerNewProject(Project projectToRegister) throws ManagerException {
        boolean isRegistered;

        Validator.validateProjectData(projectToRegister);

        try {
            isRegistered = projectDAO.insertProject(projectToRegister);

            if (!isRegistered) {
                throw new ManagerException(REGISTER_PROJECT_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(CONNECTION_ERROR_MESSAGE, exception);
        }

        return isRegistered;
    }

    public void inactivateMultipleProjects(List<Integer> projectIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = projectDAO.deactivateMultipleProjects(projectIdentifiersList);

            if (!isProcessSuccessful) {
                throw new ManagerException(INACTIVATE_PROJECTS_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(INACTIVATE_CONNECTION_ERROR_MESSAGE, exception);
        }
    }

    public List<Project> getAllProjects() throws ManagerException {
        List<Project> projects;

        try {
            projects = projectDAO.getAllProjects();
        } catch (DAOException exception) {
            throw new ManagerException(GET_ALL_PROJECTS_ERROR_MESSAGE, exception);
        }

        return projects;
    }
}