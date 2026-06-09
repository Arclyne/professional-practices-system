package mx.uv.fei.domain.manager;

import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class PracticeGroupManager {

    private static final String MSG_REGISTER_ERROR = "The practice group could not be registered in the system.";
    private static final String MSG_CONNECTION_ERROR = "A connection problem occurred. Please try again later.";
    private static final String MSG_RETRIEVE_ERROR = "An error occurred while retrieving the practice groups.";

    private final IPracticeGroupDAO practiceGroupDAO;

    @Inject
    public PracticeGroupManager(IPracticeGroupDAO practiceGroupDAO) {
        this.practiceGroupDAO = practiceGroupDAO;
    }

    public void registerNewPracticeGroup(PracticeGroup groupToRegister) throws ManagerException {

        try {
            int resultId = practiceGroupDAO.insertPracticeGroup(groupToRegister);

            if (resultId <= 0) {
                throw new ManagerException(MSG_REGISTER_ERROR);
            }
        } catch (DAOException e) {
            throw new ManagerException(MSG_CONNECTION_ERROR, e);
        }
    }

    public List<PracticeGroup> getAllPracticeGroups() throws ManagerException {
        List<PracticeGroup> groups;
        try {
            groups = practiceGroupDAO.getAllPracticeGroups();
        } catch (DAOException e) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, e);
        }
        return groups;
    }
}
