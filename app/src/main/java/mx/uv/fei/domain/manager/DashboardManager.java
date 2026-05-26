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

    public boolean isAdministratorMenuAvailable(String authenticatedUserRole) {
        return "Administrator".equalsIgnoreCase(authenticatedUserRole);
    }

    public boolean isCoordinatorMenuAvailable(String authenticatedUserRole) {
        return "Coordinator".equalsIgnoreCase(authenticatedUserRole);
    }

    public boolean isProfessorMenuAvailable(String authenticatedUserRole) {
        return "Professor".equalsIgnoreCase(authenticatedUserRole);
    }

    public boolean isPractitionerMenuAvailable(String authenticatedUserRole) {
        return "Practitioner".equalsIgnoreCase(authenticatedUserRole);
    }

    public AppSection resolvePractitionerProjectsNavigation(int practitionerIdentifier) throws ManagerException {
        AppSection resolvedTargetNavigationSection;

        try {
            boolean hasPractitionerPrioritizedProjects = postulationDAO.hasPractitionerSubmittedPriorities(practitionerIdentifier);

            if (hasPractitionerPrioritizedProjects) {
                resolvedTargetNavigationSection = AppSection.VIEW_PRACTITIONER_PRIORITIES;
            } else {
                resolvedTargetNavigationSection = AppSection.PRIORITIZE_PROJECTS;
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrio un fallo al verificar el estado de las prioridades del practicante en el servidor.", e);
        }

        return resolvedTargetNavigationSection;
    }
}