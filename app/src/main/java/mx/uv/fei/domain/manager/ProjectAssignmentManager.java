package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IPostulationDAO;
import mx.uv.fei.domain.dto.ProjectPostulation;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class ProjectAssignmentManager {

    private final IPostulationDAO postulationProjectDataAccessObject;

    @Inject
    public ProjectAssignmentManager(IPostulationDAO postulationProjectDataAccessObject) {
        this.postulationProjectDataAccessObject = postulationProjectDataAccessObject;
    }

    public List<ProjectPostulation> retrievePractitionerPostulations(int practitionerIdentifier) throws ManagerException {
        List<ProjectPostulation> retrievedPostulationsList;

        try {
            retrievedPostulationsList = postulationProjectDataAccessObject.retrievePractitionerPostulations(practitionerIdentifier);
        } catch (DAOException dataAccessObjectException) {
            throw new ManagerException("Ocurrio un problema al recuperar las postulaciones desde el servidor.", dataAccessObjectException);
        }

        return retrievedPostulationsList;
    }

    public void assignProjectToPractitioner(int practitionerIdentifier, int projectIdentifier) throws ManagerException {
        try {
            boolean isProjectAssignedSuccessfully = postulationProjectDataAccessObject.assignProjectUsingStoredProcedure(practitionerIdentifier, projectIdentifier);

            if (!isProjectAssignedSuccessfully) {
                throw new ManagerException("No fue posible asignar el proyecto. Verifique que la postulacion exista y se encuentre activa.");
            }
        } catch (DAOException dataAccessObjectException) {
            throw new ManagerException("Ocurrio un problema de conexion al intentar registrar la asignacion.", dataAccessObjectException);
        }
    }
}