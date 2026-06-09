package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.ProjectPostulation;

import java.util.List;

public interface IPostulationDAO {

    boolean hasPractitionerSubmittedPriorities(int practitionerIdentifier) throws DAOException;

    boolean insertProjectPriorities(int targetPractitionerIdentifier, List<Project> prioritizedProjectList) throws DAOException;

    List<ProjectPostulation> retrievePractitionerPostulations(int practitionerIdentifier) throws DAOException;

    boolean assignProjectUsingStoredProcedure(int practitionerIdentifier, int projectIdentifier) throws DAOException;

    boolean hasAssignedProject(int practitionerId) throws DAOException;
}