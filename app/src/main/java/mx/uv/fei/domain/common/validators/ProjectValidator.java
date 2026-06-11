package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;

public class ProjectValidator {

    public static void validateProjectData(Project projectToValidate) throws ManagerException {
        BaseValidator.validateString(projectToValidate.getProjectName(),
                "El nombre del proyecto es obligatorio.");
        BaseValidator.validateId(projectToValidate.getCompanyId(),
                "Debe seleccionar una organización vinculada.");
        BaseValidator.validateId(projectToValidate.getManagerId(),
                "Debe seleccionar un gerente para el proyecto.");
        BaseValidator.validateDateRange(projectToValidate.getStartDate(), projectToValidate.getEndDate(),
                "La fecha de inicio no puede ser mayor a la fecha final.");
    }
}