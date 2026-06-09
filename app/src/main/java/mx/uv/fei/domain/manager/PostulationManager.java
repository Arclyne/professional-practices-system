package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.dataaccess.repositories.ProjectDAO;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.ProjectPostulation;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class PostulationManager {

    private static final Logger logger = LoggerFactory.getLogger(PostulationManager.class);
    private final IPostulationDAO postulationProjectDAO;
    private final IProjectDAO projectDAO;

    @Inject
    public PostulationManager(IPostulationDAO postulationProjectDAO, ProjectDAO projectDAO) {
        this.postulationProjectDAO = postulationProjectDAO;
        this.projectDAO = projectDAO;
    }

    public List<ProjectPostulation> retrievePractitionerPostulations(int practitionerIdentifier) throws ManagerException {
        List<ProjectPostulation> retrievedPostulationsList;

        try {
            retrievedPostulationsList = postulationProjectDAO.retrievePractitionerPostulations(practitionerIdentifier);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrio un problema al recuperar las postulaciones desde el servidor.", e);
        }

        return retrievedPostulationsList;
    }

    public void assignProjectToPractitioner(int practitionerIdentifier, int projectIdentifier) throws ManagerException {
        try {
            boolean isProjectAssignedSuccessfully = postulationProjectDAO.assignProjectUsingStoredProcedure(practitionerIdentifier, projectIdentifier);

            if (!isProjectAssignedSuccessfully) {
                throw new ManagerException("No fue posible asignar el proyecto. Verifique que la postulacion exista y se encuentre activa.");
            }
        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            throw new ManagerException("Ocurrio un problema de conexion al intentar registrar la asignacion.",e);
        }
    }

    public List<Project> retrieveAllAvailableProjects() throws ManagerException {
        List<Project> availableProjectsList;

        try {
            availableProjectsList = projectDAO.getAvailableProjectsWithCapacity();
        } catch (DAOException e) {
            throw new ManagerException("Ocurrio un problema al intentar recuperar los proyectos disponibles desde el servidor.", e);
        }

        return availableProjectsList;
    }

    public void registerPractitionerPriorities(int systemPractitionerIdentifier, List<Project> prioritizedProjectsList) throws ManagerException {
        if (prioritizedProjectsList == null || prioritizedProjectsList.isEmpty()) {
            throw new ManagerException("La lista de prioridades proporcionada se encuentra vacia o corrupta.");
        } else {
            try {
                boolean arePrioritiesSavedSuccessfully = postulationProjectDAO.insertProjectPriorities(systemPractitionerIdentifier, prioritizedProjectsList);
                if (!arePrioritiesSavedSuccessfully) {
                    throw new ManagerException("No fue posible registrar las prioridades en el sistema. Intente nuevamente.");
                }
            } catch (DAOException e) {
                throw new ManagerException("Ocurrio un problema de conexion al intentar guardar sus prioridades.", e);
            }
        }
    }
}
