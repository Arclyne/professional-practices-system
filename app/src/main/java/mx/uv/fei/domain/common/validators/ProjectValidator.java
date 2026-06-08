package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;

public class ProjectValidator {
    private static final String MSG_REQ_PROJ_NAME = "El nombre del proyecto es obligatorio.";
    private static final String MSG_REQ_PROJ_ORG = "Debe seleccionar una organización vinculada.";
    private static final String MSG_REQ_PROJ_MGR = "Debes seleccionar un gerente para el proyecto.";
    private static final String MSG_REQ_PROJ_DT = "La facha de inicio no puede ser mayor a la final";

    public static void validateProjectData(Project projectToValidate) throws ManagerException {

        BaseValidator.validateString(projectToValidate.getProjectName(), MSG_REQ_PROJ_NAME);
        BaseValidator.validateId(projectToValidate.getCompanyId(), MSG_REQ_PROJ_ORG);
        BaseValidator.validateId(projectToValidate.getManagerId(), MSG_REQ_PROJ_MGR);
        BaseValidator.validateDateRange(projectToValidate.getStartDate(), projectToValidate.getEndDate(), MSG_REQ_PROJ_DT);
    }
}
