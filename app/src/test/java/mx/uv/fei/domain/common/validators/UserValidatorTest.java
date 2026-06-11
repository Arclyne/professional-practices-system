package mx.uv.fei.domain.common.validators;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

public class UserValidatorTest {

    private Practitioner buildValidPractitioner() {
        Practitioner validPractitioner = new Practitioner();
        validPractitioner.setName("Angel Gabriel");
        validPractitioner.setLastName("Aguilar Hernandez");
        validPractitioner.setPassword("ClaveSegura2026");
        validPractitioner.setGender(Gender.MALE);
        validPractitioner.setEmail("zS24242424@estudiantes.uv.mx");
        validPractitioner.setEnrollment("zs24242424");
        return validPractitioner;
    }

    @Test
    void validatePractitioner_ValidData_DoesNotThrowException() {
        Practitioner validPractitioner = buildValidPractitioner();

        assertDoesNotThrow(() -> UserValidator.validatePractitioner(validPractitioner));
    }

    @Test
    void validatePractitioner_InvalidEnrollment_ThrowsManagerException() {
        Practitioner invalidEnrollmentPractitioner = buildValidPractitioner();
        invalidEnrollmentPractitioner.setEnrollment("123");

        assertThrows(ManagerException.class, () -> UserValidator.validatePractitioner(invalidEnrollmentPractitioner));
    }
}
