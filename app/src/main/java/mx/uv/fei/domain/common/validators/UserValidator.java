package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.*;

import mx.uv.fei.domain.exceptions.ManagerException;

public class UserValidator {
    public static void validateProfessorData(Professor professor) throws ManagerException {
        validateUser(professor, "professor");
        BaseValidator.validateString(professor.getUserName(), "The personal number is required.");
        if (!BaseValidator.isValidPersonalNumber(professor.getUserName())) {
            throw new ManagerException("Invalid format: The personal number must contain only numbers.");
        }
    }

    public static void validateCoordinatorData(Coordinator coordinator) throws ManagerException {
        validateUser(coordinator, "coordinator");
        BaseValidator.validateString(coordinator.getUserName(), "The personal number is required.");
        if (!BaseValidator.isValidPersonalNumber(coordinator.getUserName())) {
            throw new ManagerException("Invalid format: The personal number must contain only numbers.");
        }
    }

    public static void validatePractitioner(Practitioner practitioner) throws ManagerException {
        validateUser(practitioner, "practitioner");
        BaseValidator.validateString(practitioner.getEnrollment(), "The practitioner's enrollment is required.");
        if (!BaseValidator.isValidEnrollment(practitioner.getEnrollment())) {
            throw new ManagerException("The provided enrollment format is invalid.");
        }
    }

    public static void validateAdministratorData(Administrator admin) throws ManagerException {
        validateUser(admin, "administrator");
        BaseValidator.validateString(admin.getUserName(), "The personal number is required.");
        if (!BaseValidator.isValidPersonalNumber(admin.getUserName())) {
            throw new ManagerException("Invalid format: The personal number must contain only numbers.");
        }
    }

    public static void validateManagerData(Manager manager) throws ManagerException {
        BaseValidator.validateString(manager.getName(), "The manager's name is required.");
        BaseValidator.validateString(manager.getPhone(), "The manager's phone is required.");
        BaseValidator.validateString(manager.getEmail(), "The manager's email is required.");
        if (!BaseValidator.isValidEmail(manager.getEmail())) {
            throw new ManagerException("The provided email format is invalid.");
        }
        BaseValidator.validateId(manager.getOrganizationId(), "You must select the organization they belong to.");
    }

    public static void validateUser(User user, String role) throws ManagerException {
        BaseValidator.validateString(user.getName(), String.format("The %s's name is required.", role));
        BaseValidator.validateString(user.getLastName(), String.format("The %s's last name is required.", role));
        PasswordValidator.validatePassword(user.getPassword());
        if (user.getGender() == null) {
            throw new ManagerException(String.format("The %s's gender is required.", role));
        }
        BaseValidator.validateString(user.getEmail(), String.format("The %s's email is required.", role));
        if (!BaseValidator.isValidEmail(user.getEmail())) {
            throw new ManagerException("The provided email format is invalid.");
        }
    }
}