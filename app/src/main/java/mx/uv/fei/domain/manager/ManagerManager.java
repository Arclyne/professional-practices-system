package mx.uv.fei.domain.manager;

import java.util.List;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IManagerDAO;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class ManagerManager {

    private final IManagerDAO managerDataAccessObject;

    @Inject
    public ManagerManager(IManagerDAO managerDataAccessObject) {
        this.managerDataAccessObject = managerDataAccessObject;
    }

    public List<Manager> getAllManagers() throws ManagerException {
        try {
            return managerDataAccessObject.getAllManagers();
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error al obtener la lista de encargados.", dataAccessException);
        }
    }

    public void inactivateMultipleManagers(List<Integer> managerIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = managerDataAccessObject.deactivateMultipleManagers(managerIdentifiersList);
            if (!isProcessSuccessful) {
                throw new ManagerException("No se pudieron inactivar los encargados seleccionados.");
            }
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error de base de datos al inactivar encargados.", dataAccessException);
        }
    }
}