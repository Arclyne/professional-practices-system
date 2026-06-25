package mx.uv.fei.domain.manager.academic;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.domain.common.PersistenceErrorTranslator;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.common.validators.FieldLengthLimits;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class PracticeGroupManager {

    private static final Logger log = LoggerFactory.getLogger(PracticeGroupManager.class);
    private final IPracticeGroupDAO practiceGroupDAO;

    @Inject
    public PracticeGroupManager(IPracticeGroupDAO practiceGroupDAO) {
        this.practiceGroupDAO = practiceGroupDAO;
    }

    public void registerNewPracticeGroup(PracticeGroup practiceGroup) throws ManagerException {
        validatePracticeGroupData(practiceGroup);

        try {
            int generatedId = practiceGroupDAO.insertPracticeGroup(practiceGroup);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo registrar el grupo de prácticas en el sistema.");
            }
        } catch (DAOException e) {
            log.error("Error al insertar el grupo de prácticas.", e);
            throw PersistenceErrorTranslator.translate(e);
        }
    }

    public List<PracticeGroup> getAllPracticeGroups() throws ManagerException {
        try {
            return practiceGroupDAO.getAllPracticeGroups();
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al recuperar los grupos de prácticas.", e);
        }
    }

    public PracticeGroup getPracticeGroupById(int groupId) throws ManagerException {
        try {
            return practiceGroupDAO.recoverPracticeGroup(groupId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo recuperar la información del grupo de prácticas.", e);
        }
    }

    public void updatePracticeGroup(PracticeGroup practiceGroup, int groupId) throws ManagerException {
        validatePracticeGroupData(practiceGroup);

        try {
            practiceGroupDAO.updatePracticeGroup(practiceGroup, groupId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un problema de conexión. Por favor, intente más tarde.", e);
        }
    }

    private void validatePracticeGroupData(PracticeGroup practiceGroup) throws ManagerException {
        BaseValidator.validateString(practiceGroup.getSection(),
                "El NRC del grupo de prácticas es obligatorio.");
        BaseValidator.validateNumeric(practiceGroup.getSection(),
                "El NRC del grupo de prácticas debe contener únicamente números.");
        BaseValidator.validateMaxLength(practiceGroup.getSection(), FieldLengthLimits.SECTION_MAX,
                "El NRC no puede exceder " + FieldLengthLimits.SECTION_MAX + " caracteres.");
        BaseValidator.validateId(practiceGroup.getPeriodId(),
                "Debe seleccionar el periodo escolar del grupo.");
        BaseValidator.validateId(practiceGroup.getProfessorId(),
                "Debe seleccionar el académico responsable del grupo.");
    }
}