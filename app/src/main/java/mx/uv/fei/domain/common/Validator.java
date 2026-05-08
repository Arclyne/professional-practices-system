package mx.uv.fei.domain.common;

import mx.uv.fei.domain.dto.*;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.Date;
import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern ENROLLMENT_PATTERN = Pattern.compile("^(zs|s)[0-9]{8}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidEnrollment(String enrollment) {
        return enrollment != null && ENROLLMENT_PATTERN.matcher(enrollment).matches();
    }

    public static void validateActivityData(Activity activityToValidate) throws ManagerException {
        validateString(activityToValidate.getName(), "El nombre de la actividad es obligatorio.");
        validateString(activityToValidate.getManager(), "El encargado de la actividad es obligatorio.");
        validateDateRange(activityToValidate.getStartDate(), activityToValidate.getEndDate());
    }

    public static void validateProjectData(Project projectToValidate) throws ManagerException {
        validateString(projectToValidate.getProjectName(), "El nombre del proyecto es obligatorio.");
        validateId(projectToValidate.getCompanyId(), "Debe seleccionar una organización vinculada.");
        validateId(projectToValidate.getManagerId(), "Debe seleccionar un encargado para el proyecto.");

        validateDateRange(projectToValidate.getStartDate(), projectToValidate.getEndDate());
    }

    public static void validateProfessorData(Professor professorToValidate) throws ManagerException {
        validateUser(professorToValidate, "profesor");
        validateString(professorToValidate.getUserName(), "El No. personal es obligatorio.");
    }

    public static void validateCoordinatorData(Coordinator coordinatorToValidate) throws ManagerException {
        validateUser(coordinatorToValidate, "coordinador");
        validateString(coordinatorToValidate.getUserName(), "El No. personal es obligatorio.");
    }

    public static void validatePractitioner(Practitioner practitionerToValidate) throws ManagerException {
        validateUser(practitionerToValidate, "practicante");
        validateString(practitionerToValidate.getEnrollment(), "La matrícula del practicante es obligatoria.");

        if (!isValidEnrollment(practitionerToValidate.getEnrollment())) {
            throw new ManagerException("El formato de la matrícula proporcionada no es válido.");
        }
    }

    public static void validateAdministratorData(Administrator administratorToValidate) throws ManagerException {
        validateUser(administratorToValidate, "administrador");
        validateString(administratorToValidate.getUserName(), "El No. personal es obligatorio.");
    }

    private static void validateUser(User userToValidate, String rol) throws ManagerException {
        validateString(userToValidate.getName(), "El nombre del " + rol + " es obligatorio.");
        validateString(userToValidate.getLastName(), "Los apellidos del " + rol + " son obligatorios.");
        validateString(userToValidate.getGender(), "El género del " + rol + " es obligatorio.");
        validateString(userToValidate.getEmail(), "El correo del " + rol + " es obligatorio.");

        if (!isValidEmail(userToValidate.getEmail())) {
            throw new ManagerException("El formato del correo electrónico proporcionado no es válido.");
        }
    }

    private static void validateId(int id, String errorMessage) throws ManagerException {
        if (id <= 0) {
            throw new ManagerException(errorMessage);
        }
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