package mx.uv.fei.dataaccess.interfaces;

import java.util.List;
import java.util.Map;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.Project;

public interface IProjectDAO {
    int insertProject(Project project) throws DAOException;

    Project recoverProject(String projectName, int managerId) throws DAOException;

    List<Project> getAllProjects() throws DAOException;

    void updateProject(Project projectToUpdate, int ID) throws DAOException;

    List<Project> getAvailableProjectsWithCapacity() throws DAOException;

    void deactivateMultipleProjects(List<Integer> projectIdentifiersList) throws DAOException;

    void activateProject(int projectId) throws DAOException;

    Project getAssignedProjectByPractitioner(int practitionerId) throws DAOException;

    Map<Integer, Integer> getAssignedCountsByProject() throws DAOException;

}
