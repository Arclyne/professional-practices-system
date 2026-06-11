package mx.uv.fei.domain.common.validators;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

public class BaseValidatorTest {

    @Test
    void isValidEmail_ValidEmail_ReturnsTrue() {
        assertTrue(BaseValidator.isValidEmail("zS24242424@estudiantes.uv.mx"));
    }

    @Test
    void isValidEmail_InvalidEmail_ReturnsFalse() {
        assertFalse(BaseValidator.isValidEmail("correo.sin.arroba.uv.mx"));
    }

    @Test
    void isValidEnrollment_ValidEnrollment_ReturnsTrue() {
        assertTrue(BaseValidator.isValidEnrollment("s24242424"));
    }

    @Test
    void isValidEnrollment_InvalidEnrollment_ReturnsFalse() {
        assertFalse(BaseValidator.isValidEnrollment("24242424"));
    }

    @Test
    void validateString_EmptyValue_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> BaseValidator.validateString("", "El nombre no puede estar vacio"));
    }

    @Test
    void validateId_NegativeId_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> BaseValidator.validateId(-1, "El identificador debe ser positivo"));
    }
}
