package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IPostulationDAO;
import mx.uv.fei.dataacces.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class ProjectPrioritizationManager {

    private final IProjectDAO projectDAO;
    private final IPostulationDAO postulationProjectDAO;

    @Inject
    public ProjectPrioritizationManager(IProjectDAO projectDAO, IPostulationDAO postulationProjectDAO) {
        this.projectDAO = projectDAO;
        this.postulationProjectDAO = postulationProjectDAO;
    }

    public List<Project> retrieveAllAvailableProjects() throws ManagerException {
        List<Project> availableProjectsList;

        try {
            availableProjectsList = projectDAO.getAvailableProjectsWithCapacity();
        } catch (DAOException dataAccessObjectException) {
            throw new ManagerException("Ocurrio un problema al intentar recuperar los proyectos disponibles desde el servidor.", dataAccessObjectException);
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
            } catch (DAOException dataAccessObjectException) {
                throw new ManagerException("Ocurrio un problema de conexion al intentar guardar sus prioridades.", dataAccessObjectException);
            }
        }
    }
}