package mx.uv.fei.domain.manager;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProjectDAO;
import mx.uv.fei.dataacces.repositories.ActivityDAO;
import mx.uv.fei.dataacces.repositories.ProjectDAO;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.exceptions.ManagerException;

public class ProjectManager {

    private final IActivityDAO activityDataAccessObject;
    private final IProjectDAO projectDataAccessObject;

    public ProjectManager(IDatabaseConnection databaseConnection) {
        this.activityDataAccessObject = new ActivityDAO(databaseConnection);
        this.projectDataAccessObject = new ProjectDAO(databaseConnection);
    }

    public boolean registerNewActivity(Activity activityToRegister) throws ManagerException {
        Validator.validateActivityData(activityToRegister);

        try {
            boolean isRegistered = activityDataAccessObject.insertActivity(activityToRegister);

            if (!isRegistered) {
                throw new ManagerException("No se pudo completar el registro de la actividad en el sistema.");
            }
            return true;

        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema. Por favor, intente más tarde.", e);
        }
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
}