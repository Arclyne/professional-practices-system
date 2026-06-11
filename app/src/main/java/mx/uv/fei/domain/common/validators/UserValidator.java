package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;

public class UserValidator {

    public static void validateProfessorData(Professor professor) throws ManagerException {
        validateUser(professor, "el profesor");
        validatePersonalNumber(professor.getUserName());
    }

    public static void validateCoordinatorData(Coordinator coordinator) throws ManagerException {
        validateUser(coordinator, "el coordinador");
        validatePersonalNumber(coordinator.getUserName());
    }

    public static void validateAdministratorData(Administrator administrator) throws ManagerException {
        validateUser(administrator, "el administrador");
        validatePersonalNumber(administrator.getUserName());
    }

    public static void validatePractitioner(Practitioner practitioner) throws ManagerException {
        validateUser(practitioner, "el practicante");
        BaseValidator.validateString(practitioner.getEnrollment(),
                "La matrícula del practicante es obligatoria.");
        if (!BaseValidator.isValidEnrollment(practitioner.getEnrollment())) {
            throw new ManagerException("El formato de matrícula proporcionado no es válido.");
        }
    }

    public static void validateManagerData(Manager manager) throws ManagerException {
        BaseValidator.validateString(manager.getName(),
                "El nombre del encargado es obligatorio.");
        BaseValidator.validateString(manager.getPhone(),
                "El teléfono del encargado es obligatorio.");
        BaseValidator.validateString(manager.getEmail(),
                "El correo electrónico del encargado es obligatorio.");
        if (!BaseValidator.isValidEmail(manager.getEmail())) {
            throw new ManagerException("El formato del correo electrónico proporcionado no es válido.");
        }
        BaseValidator.validateId(manager.getOrganizationId(),
                "Debe seleccionar la organización a la que pertenece.");
    }

    public static void validateUser(User user, String roleLabel) throws ManagerException {
        BaseValidator.validateString(user.getName(),
                "El nombre de " + roleLabel + " es obligatorio.");
        BaseValidator.validateString(user.getLastName(),
                "El apellido de " + roleLabel + " es obligatorio.");
        PasswordValidator.validatePassword(user.getPassword());
        if (user.getGender() == null) {
            throw new ManagerException("El género de " + roleLabel + " es obligatorio.");
        }
        BaseValidator.validateString(user.getEmail(),
                "El correo electrónico de " + roleLabel + " es obligatorio.");
        if (!BaseValidator.isValidEmail(user.getEmail())) {
            throw new ManagerException("El formato del correo electrónico proporcionado no es válido.");
        }
    }

    private static void validatePersonalNumber(String personalNumber) throws ManagerException {
        BaseValidator.validateString(personalNumber,
                "El número de personal es obligatorio.");
        if (!BaseValidator.isValidPersonalNumber(personalNumber)) {
            throw new ManagerException("Formato inválido: el número de personal debe contener solo números.");
        }
    }
}