package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.dto.ProjectPostulation;
import mx.uv.fei.domain.exceptions.ManagerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class PostulationManager {

    private static final Logger log = LoggerFactory.getLogger(PostulationManager.class);

    private final IPostulationDAO postulationDAO;
    private final IProjectDAO projectDAO;

    @Inject
    public PostulationManager(IPostulationDAO postulationDAO, IProjectDAO projectDAO) {
        this.postulationDAO = postulationDAO;
        this.projectDAO = projectDAO;
    }

    public List<ProjectPostulation> retrievePractitionerPostulations(int practitionerId) throws ManagerException {
        try {
            return postulationDAO.retrievePractitionerPostulations(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema al recuperar las postulaciones.", e);
        }
    }

    public void assignProjectToPractitioner(int practitionerId, int projectId) throws ManagerException {
        try {
            boolean isAssigned = postulationDAO.assignProjectUsingStoredProcedure(practitionerId, projectId);
            if (!isAssigned) {
                throw new ManagerException("No fue posible asignar el proyecto. Verifique que la postulación exista y se encuentre activa.");
            }
        } catch (DAOException e) {
            log.error("Error al asignar el proyecto {} al practicante {}.", projectId, practitionerId, e);
            throw new ManagerException("Ocurrió un problema de conexión al intentar registrar la asignación.", e);
        }
    }

    public List<Project> retrieveAllAvailableProjects() throws ManagerException {
        try {
            return projectDAO.getAvailableProjectsWithCapacity();
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema al intentar recuperar los proyectos disponibles.", e);
        }
    }

    public void registerPractitionerPriorities(int practitionerId, List<Project> prioritizedProjects) throws ManagerException {
        if (prioritizedProjects == null || prioritizedProjects.isEmpty()) {
            throw new ManagerException("La lista de prioridades proporcionada se encuentra vacía.");
        }
        try {
            boolean arePrioritiesSaved = postulationDAO.insertProjectPriorities(practitionerId, prioritizedProjects);
            if (!arePrioritiesSaved) {
                throw new ManagerException("No fue posible registrar las prioridades en el sistema. Intente nuevamente.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión al intentar guardar las prioridades.", e);
        }
    }

    public Project getAssignedProject(int practitionerId) throws ManagerException {
        try {
            return projectDAO.getAssignedProjectByPractitioner(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo recuperar el proyecto asignado.", e);
        }
    }
}