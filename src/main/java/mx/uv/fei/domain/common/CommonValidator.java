package mx.uv.fei.domain.common;

import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.Project;

import java.util.Map;

public class CommonValidator {

    public static void validateActivityData(Activity activityToValidate) {
        if (activityToValidate.getName() == null || activityToValidate.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la actividad es obligatorio.");
        }

        if (activityToValidate.getManager() == null || activityToValidate.getManager().trim().isEmpty()) {
            throw new IllegalArgumentException("El encargado de la actividad es obligatorio.");
        }

        if (activityToValidate.getStartDate() != null && activityToValidate.getEndDate() != null) {
            if (activityToValidate.getEndDate().before(activityToValidate.getStartDate())) {
                throw new IllegalArgumentException("La fecha de término no puede ser anterior a la fecha de inicio.");
            }
        }
    }

    public static void validateProjectData(Project projectToValidate) {
        if (projectToValidate.getProjectName() == null || projectToValidate.getProjectName().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proyecto es obligatorio.");
        }

        if (projectToValidate.getManager() == null || projectToValidate.getManager().trim().isEmpty()) {
            throw new IllegalArgumentException("El encargado del proyecto es obligatorio.");
        }

        if (projectToValidate.getStartDate() != null && projectToValidate.getEndDate() != null) {
            if (projectToValidate.getEndDate().before(projectToValidate.getStartDate())) {
                throw new IllegalArgumentException("La fecha de término no puede ser anterior a la fecha de inicio.");
            }
        }
    }

    public static void validateNoEmptyString(String stringToValidate, String messeng) {
        if (stringToValidate == null || stringToValidate.trim().isEmpty()) {
            throw new IllegalArgumentException(messeng);
        }
    }

    public static void validateNoEmptyListString(Map<String, String> listToCheck) {
        for (Map.Entry<String, String> stringToCheck : listToCheck.entrySet()) {
            validateNoEmptyString(stringToCheck.getKey(), stringToCheck.getValue());
        }
    }

}
