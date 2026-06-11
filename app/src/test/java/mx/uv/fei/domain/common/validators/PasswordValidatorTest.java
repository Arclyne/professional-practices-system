package mx.uv.fei.domain.common.validators;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

public class PasswordValidatorTest {

    @Test
    void validatePassword_ValidPassword_DoesNotThrowException() {
        assertDoesNotThrow(() -> PasswordValidator.validatePassword("ClaveSegura2026"));
    }

    @Test
    void validatePassword_NullPassword_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordValidator.validatePassword(null));
    }

    @Test
    void validatePassword_ShortPassword_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordValidator.validatePassword("Cl4"));
    }

    @Test
    void validatePassword_NoUppercase_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordValidator.validatePassword("clavesegura2026"));
    }

    @Test
    void validatePassword_NoNumber_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> PasswordValidator.validatePassword("ClaveSegura"));
    }
}
