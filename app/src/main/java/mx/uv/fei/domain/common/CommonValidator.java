package mx.uv.fei.domain.common;

import mx.uv.fei.domain.dto.*;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.Date;

public class CommonValidator {

    public static void validateActivityData(Activity activityToValidate) throws ManagerException {
        validateString(activityToValidate.getName(), "El nombre de la actividad es obligatorio.");
        validateString(activityToValidate.getManager(), "El encargado de la actividad es obligatorio.");
        validateDateRange(activityToValidate.getStartDate(), activityToValidate.getEndDate());
    }

    public static void validateProjectData(Project projectToValidate) throws ManagerException {
        validateString(projectToValidate.getProjectName(), "El nombre del proyecto es obligatorio.");
        validateString(projectToValidate.getManager(), "El encargado del proyecto es obligatorio.");
        validateDateRange(projectToValidate.getStartDate(), projectToValidate.getEndDate());
    }

    public static void validateProfessorData(Professor professorToValidate) throws ManagerException {
        validateUser(professorToValidate, "profesor");
        validateString(professorToValidate.getUserName(), "El No. personal es obligatorio");
    }

    public static void validatePractitioner(Practitioner practitionerToValidate) throws ManagerException {
        validateUser(practitionerToValidate, "practicante");
        validateString(practitionerToValidate.getEnrollment(), "La matricula del practicante es obligatoria");
    }

    private static void validateUser(User userToValidate, String rol) throws ManagerException {
        validateString(userToValidate.getName(), "El nombre del " + rol + " es obligatorio.");
        validateString(userToValidate.getLastName(), "Los apellidos del " + rol + " son obligatorios.");
        validateString(userToValidate.getGender(), "El género del " + rol + " es obligatorio.");
        validateString(userToValidate.getEmail(), "El Correo del " + rol + "es obligatorio");
    }

    private static void validateString(String value, String errorMessage) throws ManagerException {
        if (value == null || value.trim().isEmpty()) {
            throw new ManagerException(errorMessage);
        }
    }

    private static void validateDateRange(Date startDate, Date endDate) throws ManagerException {
        if (startDate != null && endDate != null && endDate.before(startDate)) {
            throw new ManagerException("La fecha de término no puede ser anterior a la fecha de inicio.");
        }
    }
}