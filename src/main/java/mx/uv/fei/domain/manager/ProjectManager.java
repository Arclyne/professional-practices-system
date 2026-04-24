package mx.uv.fei.domain.manager;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProjectDAO;
import mx.uv.fei.dataacces.repositories.ActivityDAO;
import mx.uv.fei.dataacces.repositories.ProjectDAO;
import mx.uv.fei.dataacces.exceptions.DAOException;

public class ProjectManager {

    private final IActivityDAO activityDAO;
    private final IProjectDAO projectDAO;

    public ProjectManager(IDatabaseConnection databaseConnection) {
        this.activityDAO = new ActivityDAO(databaseConnection);
        this.projectDAO = new ProjectDAO(databaseConnection);
    }

    public boolean registerNewActivity(Activity activityToRegister) throws DAOException {
        validateActivityData(activityToRegister);
        return activityDAO.insertActivity(activityToRegister);
    }

    public boolean registerNewProject(Project projectToRegister) throws DAOException {
        validateProjectData(projectToRegister);
        return projectDAO.insertProject(projectToRegister);
    }

    private void validateActivityData(Activity activityToValidate) {
        if (activityToValidate.getName() == null || activityToValidate.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio.");
        }

        if (activityToValidate.getManager() == null || activityToValidate.getManager().trim().isEmpty()) {
            throw new IllegalArgumentException("El encargado de la actividad es obligatorio.");
        }

        if (activityToValidate.getStartDate() != null && activityToValidate.getEndDate() != null) {
            if (activityToValidate.getEndDate().before(activityToValidate.getStartDate())) {
                throw new IllegalArgumentException("La fecha de término no puede ser anterior a la fecha de inicio.");
            }
        }
    }

    private void validateProjectData(Project projectToValidate) {
        if (projectToValidate.getProjectName() == null || projectToValidate.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio.");
        }

        if (projectToValidate.getManager() == null || projectToValidate.getManager().trim().isEmpty()) {
            throw new IllegalArgumentException("El encargado de la actividad es obligatorio.");
        }

        if (projectToValidate.getStartDate() != null && projectToValidate.getEndDate() != null) {
            if (projectToValidate.getEndDate().before(projectToValidate.getStartDate())) {
                throw new IllegalArgumentException("La fecha de término no puede ser anterior a la fecha de inicio.");
            }
        }
    }
}