package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordValidatorTest {

    @Test
    void validatePassword_ValidPassword_DoesNotThrowException() {
        assertDoesNotThrow(() -> PasswordValidator.validatePassword("Password123"));
    }

    @Test
    void validatePassword_NullPassword_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordValidator.validatePassword(null));
    }

    @Test
    void validatePassword_ShortPassword_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordValidator.validatePassword("P1"));
    }

    @Test
    void validatePassword_NoUppercase_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordValidator.validatePassword("password123"));
    }

    @Test
    void validatePassword_NoNumber_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordValidator.validatePassword("Password"));
    }
}