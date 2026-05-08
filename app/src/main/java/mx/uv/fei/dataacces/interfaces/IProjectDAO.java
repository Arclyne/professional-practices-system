package mx.uv.fei.dataacces.interfaces;

import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Project;

public interface IProjectDAO {
    boolean insertProject(Project project) throws DAOException;

    Project recoverProject(String projectName, int managerId) throws DAOException;

    List<Project> getAllProjects() throws DAOException;

    boolean updateProject(Project projectToUpdate, int ID) throws DAOException;
}
