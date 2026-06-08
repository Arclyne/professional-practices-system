package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.regex.Pattern;

public class PasswordValidator {
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_NUMBER = Pattern.compile("[0-9]");

    public static void validatePassword(String password) throws ManagerException {
        if (password == null || password.isBlank()) {
            throw new ManagerException("The password is required and cannot be empty.");
        }
        if (password.length() < 8) {
            throw new ManagerException("The password must be at least 8 characters long.");
        }
        if (!HAS_UPPERCASE.matcher(password).find()) {
            throw new ManagerException("The password must contain at least one uppercase letter.");
        }
        if (!HAS_NUMBER.matcher(password).find()) {
            throw new ManagerException("The password must contain at least one number.");
        }
    }
}
