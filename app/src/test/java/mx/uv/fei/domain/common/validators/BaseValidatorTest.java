package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BaseValidatorTest {

    @Test
    void isValidEmail_ValidEmail_ReturnsTrue() {
        assertTrue(BaseValidator.isValidEmail("test@uv.mx"));
    }

    @Test
    void isValidEmail_InvalidEmail_ReturnsFalse() {
        assertFalse(BaseValidator.isValidEmail("testuv.mx"));
    }

    @Test
    void isValidEnrollment_ValidEnrollment_ReturnsTrue() {
        assertTrue(BaseValidator.isValidEnrollment("s12345678"));
    }

    @Test
    void isValidEnrollment_InvalidEnrollment_ReturnsFalse() {
        assertFalse(BaseValidator.isValidEnrollment("12345678"));
    }

    @Test
    void validateString_EmptyValue_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> BaseValidator.validateString("", "Error"));
    }

    @Test
    void validateId_NegativeId_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> BaseValidator.validateId(-1, "Error"));
    }
}