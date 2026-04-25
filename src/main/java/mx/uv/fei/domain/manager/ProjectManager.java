package mx.uv.fei.domain.manager;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProjectDAO;
import mx.uv.fei.dataacces.repositories.ActivityDAO;
import mx.uv.fei.dataacces.repositories.ProjectDAO;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.common.CommonValidator;

public class ProjectManager {

    private final IActivityDAO activityDataAccessObject;
    private final IProjectDAO projectDataAccessObject;

    public ProjectManager(IDatabaseConnection DatabaseConnection) {
        this.activityDataAccessObject = new ActivityDAO(DatabaseConnection);
        this.projectDataAccessObject = new ProjectDAO(DatabaseConnection);
    }

    public boolean registerNewActivity(Activity activityToRegister) throws DAOException {
        CommonValidator.validateActivityData(activityToRegister);
        return activityDataAccessObject.insertActivity(activityToRegister);
    }

    public boolean registerNewProject(Project projectToRegister) throws DAOException {
        CommonValidator.validateProjectData(projectToRegister);
        return projectDataAccessObject.insertProject(projectToRegister);
    }

}