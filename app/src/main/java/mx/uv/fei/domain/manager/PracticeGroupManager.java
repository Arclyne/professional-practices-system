package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.List;

@Component
public class PracticeGroupManager {

    private final IPracticeGroupDAO practiceGroupDAO;

    @Inject
    public PracticeGroupManager(IPracticeGroupDAO practiceGroupDAO) {
        this.practiceGroupDAO = practiceGroupDAO;
    }

    public void registerNewPracticeGroup(PracticeGroup practiceGroup) throws ManagerException {
        try {
            int generatedId = practiceGroupDAO.insertPracticeGroup(practiceGroup);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo registrar el grupo de prácticas en el sistema.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    public List<PracticeGroup> getAllPracticeGroups() throws ManagerException {
        try {
            return practiceGroupDAO.getAllPracticeGroups();
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al recuperar los grupos de prácticas.", e);
        }
    }
}