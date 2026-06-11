package mx.uv.fei.dataaccess.interfaces;

import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.Project;

public interface IProjectDAO {
    boolean insertProject(Project project) throws DAOException;

    Project recoverProject(String projectName, int managerId) throws DAOException;

    List<Project> getAllProjects() throws DAOException;

    boolean updateProject(Project projectToUpdate, int ID) throws DAOException;

    List<Project> getAvailableProjectsWithCapacity() throws DAOException;

    boolean deactivateMultipleProjects(List<Integer> projectIdentifiersList) throws DAOException;

    Project getAssignedProjectByPractitioner(int practitionerId) throws DAOException;

}
