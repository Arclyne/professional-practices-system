package mx.uv.fei.domain.manager;

import java.util.List;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;


@Component
public class OrganizationManager {

    private static final String REGISTER_ORG_ERROR_MESSAGE = "No se pudo completar el registro de la organización en el sistema.";
    private static final String CONNECTION_ERROR_MESSAGE = "Ocurrió un problema de conexión. Por favor, intente más tarde.";
    private static final String GET_ALL_ORG_ERROR_MESSAGE = "Error al recuperar la lista de organizaciones.";
    private static final String INACTIVATE_ORG_ERROR_MESSAGE = "No se pudieron inactivar las organizaciones seleccionadas.";
    private static final String INACTIVATE_CONNECTION_ERROR_MESSAGE = "Error de base de datos al inactivar organizaciones.";

    private final IOrganizationDAO organizationDAO;

    @Inject
    public OrganizationManager(IOrganizationDAO organizationDAO) {
        this.organizationDAO = organizationDAO;
    }

    public boolean registerOrganization(Organization organizationToRegister) throws ManagerException {
        boolean isRegistered;

        try {
            isRegistered = organizationDAO.insertOrganization(organizationToRegister);

            if (!isRegistered) {
                throw new ManagerException(REGISTER_ORG_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(CONNECTION_ERROR_MESSAGE, exception);
        }

        return isRegistered;
    }

    public List<Organization> getAllOrganizations() throws ManagerException {
        List<Organization> organizations;

        try {
            organizations = organizationDAO.getAllOrganization();
        } catch (DAOException exception) {
            throw new ManagerException(GET_ALL_ORG_ERROR_MESSAGE, exception);
        }

        return organizations;
    }

    public void inactivateMultipleOrganizations(List<Integer> organizationIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = organizationDAO.deactivateMultipleOrganizations(organizationIdentifiersList);

            if (!isProcessSuccessful) {
                throw new ManagerException(INACTIVATE_ORG_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(INACTIVATE_CONNECTION_ERROR_MESSAGE, exception);
        }
    }
}