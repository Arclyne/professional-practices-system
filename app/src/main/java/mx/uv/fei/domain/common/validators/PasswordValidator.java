package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.regex.Pattern;

public class PasswordValidator {

    private static final int MINIMUM_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_LENGTH = 50;

    private static final Pattern UPPERCASE_LETTER_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");

    public static void validatePassword(String password) throws ManagerException {
        if (password == null || password.isBlank()) {
            throw new ManagerException("La contraseña es requerida y no puede estar vacía.");
        }
        if (password.length() < MINIMUM_PASSWORD_LENGTH) {
            throw new ManagerException("La contraseña debe tener al menos " + MINIMUM_PASSWORD_LENGTH + " caracteres.");
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            throw new ManagerException("La contraseña es demasiado larga " + MAX_PASSWORD_LENGTH + " caracteres máximo.");
        }
        if (!UPPERCASE_LETTER_PATTERN.matcher(password).find()) {
            throw new ManagerException("La contraseña debe contener al menos una letra mayúscula.");
        }
        if (!DIGIT_PATTERN.matcher(password).find()) {
            throw new ManagerException("La contraseña debe contener al menos un número.");
        }
    }
}