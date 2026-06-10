package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPostulationDAO;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class DashboardManager {

    private final IPostulationDAO postulationDAO;

    @Inject
    public DashboardManager(IPostulationDAO postulationDAO) {
        this.postulationDAO = postulationDAO;
    }

    public boolean isAdministratorMenuAvailable(String userRole) {
        return "Administrator".equalsIgnoreCase(userRole);
    }

    public boolean isCoordinatorMenuAvailable(String userRole) {
        return "Coordinator".equalsIgnoreCase(userRole);
    }

    public boolean isProfessorMenuAvailable(String userRole) {
        return "Professor".equalsIgnoreCase(userRole);
    }

    public boolean isPractitionerMenuAvailable(String userRole) {
        return "Practitioner".equalsIgnoreCase(userRole);
    }

    public AppSection resolvePractitionerProjectsNavigation(int practitionerId) throws ManagerException {
        AppSection targetSection;

        try {
            boolean hasSubmittedPriorities = postulationDAO.hasPractitionerSubmittedPriorities(practitionerId);
            targetSection = hasSubmittedPriorities
                    ? AppSection.VIEW_PRACTITIONER_PRIORITIES
                    : AppSection.PRIORITIZE_PROJECTS;
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un fallo al verificar el estado de las prioridades del practicante.", e);
        }

        return targetSection;
    }
}