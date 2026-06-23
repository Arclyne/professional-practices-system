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
        BaseValidator.validateMaxLength(practitioner.getIndigenousLanguage(), FieldLengthLimits.INDIGENOUS_LANGUAGE_MAX,
                "La lengua indígena no puede exceder " + FieldLengthLimits.INDIGENOUS_LANGUAGE_MAX + " caracteres.");
    }

    public static void validatePractitionerText(Practitioner practitioner) throws ManagerException {
        BaseValidator.validateString(practitioner.getName(),
                "El nombre del practicante es obligatorio.");
        if (!BaseValidator.isValidName(practitioner.getName())) {
            throw new ManagerException("El nombre del practicante contiene caracteres inválidos.");
        }
        BaseValidator.validateMaxLength(practitioner.getName(), FieldLengthLimits.NAME_MAX,
                "El nombre del practicante no puede exceder " + FieldLengthLimits.NAME_MAX + " caracteres.");

        BaseValidator.validateString(practitioner.getLastName(),
                "El apellido del practicante es obligatorio.");
        if (!BaseValidator.isValidName(practitioner.getLastName())) {
            throw new ManagerException("El apellido del practicante contiene caracteres inválidos.");
        }
        BaseValidator.validateMaxLength(practitioner.getLastName(), FieldLengthLimits.LAST_NAME_MAX,
                "El apellido del practicante no puede exceder " + FieldLengthLimits.LAST_NAME_MAX + " caracteres.");

        BaseValidator.validateString(practitioner.getEmail(),
                "El correo electrónico del practicante es obligatorio.");
        BaseValidator.validateMaxLength(practitioner.getEmail(), FieldLengthLimits.EMAIL_MAX,
                "El correo electrónico del practicante no puede exceder " + FieldLengthLimits.EMAIL_MAX + " caracteres.");
        if (!BaseValidator.isValidEmail(practitioner.getEmail())) {
            throw new ManagerException("El formato del correo electrónico proporcionado no es válido.");
        }
        BaseValidator.validateString(practitioner.getEnrollment(),
                "La matrícula del practicante es obligatoria.");
        if (!BaseValidator.isValidEnrollment(practitioner.getEnrollment())) {
            throw new ManagerException("El formato de matrícula proporcionado no es válido.");
        }
        BaseValidator.validateMaxLength(practitioner.getIndigenousLanguage(), FieldLengthLimits.INDIGENOUS_LANGUAGE_MAX,
                "La lengua indígena no puede exceder " + FieldLengthLimits.INDIGENOUS_LANGUAGE_MAX + " caracteres.");
    }

    public static void validateManagerData(Manager manager) throws ManagerException {
        BaseValidator.validateString(manager.getName(),
                "El nombre del encargado es obligatorio.");
        if (!BaseValidator.isValidName(manager.getName())) {
            throw new ManagerException("El nombre del encargado contiene caracteres inválidos.");
        }
        BaseValidator.validateMaxLength(manager.getName(), FieldLengthLimits.MANAGER_NAME_MAX,
                "El nombre del encargado no puede exceder " + FieldLengthLimits.MANAGER_NAME_MAX + " caracteres.");

        BaseValidator.validateString(manager.getPhone(),
                "El teléfono del encargado es obligatorio.");
        BaseValidator.validateMaxLength(manager.getPhone(), FieldLengthLimits.PHONE_MAX,
                "El teléfono del encargado no puede exceder " + FieldLengthLimits.PHONE_MAX + " caracteres.");

        BaseValidator.validateString(manager.getEmail(),
                "El correo electrónico del encargado es obligatorio.");
        BaseValidator.validateMaxLength(manager.getEmail(), FieldLengthLimits.EMAIL_MAX,
                "El correo electrónico del encargado no puede exceder " + FieldLengthLimits.EMAIL_MAX + " caracteres.");
        if (!BaseValidator.isValidEmail(manager.getEmail())) {
            throw new ManagerException("El formato del correo electrónico proporcionado no es válido.");
        }
        BaseValidator.validateId(manager.getOrganizationId(),
                "Debe seleccionar la organización a la que pertenece.");
    }

    public static void validateUser(User user, String roleLabel) throws ManagerException {
        validateUserPersonalData(user, roleLabel);
        PasswordValidator.validatePassword(user.getPassword());
    }

    public static void validateProfessorForUpdate(Professor professor) throws ManagerException {
        validateUserPersonalData(professor, "el profesor");
    }

    public static void validateCoordinatorForUpdate(Coordinator coordinator) throws ManagerException {
        validateUserPersonalData(coordinator, "el coordinador");
    }

    public static void validatePractitionerForUpdate(Practitioner practitioner) throws ManagerException {
        validateUserPersonalData(practitioner, "el practicante");
        BaseValidator.validateMaxLength(practitioner.getIndigenousLanguage(), FieldLengthLimits.INDIGENOUS_LANGUAGE_MAX,
                "La lengua indígena no puede exceder " + FieldLengthLimits.INDIGENOUS_LANGUAGE_MAX + " caracteres.");
    }

    private static void validateUserPersonalData(User user, String roleLabel) throws ManagerException {
        BaseValidator.validateString(user.getName(),
                "El nombre de " + roleLabel + " es obligatorio.");
        if (!BaseValidator.isValidName(user.getName())) {
            throw new ManagerException("El nombre de " + roleLabel + " contiene caracteres inválidos.");
        }
        BaseValidator.validateMaxLength(user.getName(), FieldLengthLimits.NAME_MAX,
                "El nombre de " + roleLabel + " no puede exceder " + FieldLengthLimits.NAME_MAX + " caracteres.");

        BaseValidator.validateString(user.getLastName(),
                "El apellido de " + roleLabel + " es obligatorio.");
        if (!BaseValidator.isValidName(user.getLastName())) {
            throw new ManagerException("El apellido de " + roleLabel + " contiene caracteres inválidos.");
        }
        BaseValidator.validateMaxLength(user.getLastName(), FieldLengthLimits.LAST_NAME_MAX,
                "El apellido de " + roleLabel + " no puede exceder " + FieldLengthLimits.LAST_NAME_MAX + " caracteres.");

        if (user.getGender() == null) {
            throw new ManagerException("El género de " + roleLabel + " es obligatorio.");
        }
        BaseValidator.validateString(user.getEmail(),
                "El correo electrónico de " + roleLabel + " es obligatorio.");
        BaseValidator.validateMaxLength(user.getEmail(), FieldLengthLimits.EMAIL_MAX,
                "El correo electrónico de " + roleLabel + " no puede exceder " + FieldLengthLimits.EMAIL_MAX + " caracteres.");
        if (!BaseValidator.isValidEmail(user.getEmail())) {
            throw new ManagerException("El formato del correo electrónico proporcionado no es válido.");
        }
    }

    private static void validatePersonalNumber(String personalNumber) throws ManagerException {
        BaseValidator.validateString(personalNumber,
                "El número de personal es obligatorio.");
        BaseValidator.validateMaxLength(personalNumber, FieldLengthLimits.USERNAME_MAX,
                "El número de personal no puede exceder " + FieldLengthLimits.USERNAME_MAX + " caracteres.");
        if (!BaseValidator.isValidPersonalNumber(personalNumber)) {
            throw new ManagerException("Formato inválido: el número de personal debe contener solo números.");
        }
    }
}