package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserValidatorTest {

    @Test
    void validatePractitioner_ValidData_DoesNotThrowException() {
        Practitioner p = new Practitioner();
        p.setName("Angel");
        p.setLastName("Aguilar");
        p.setPassword("Password123");
        p.setGender(Gender.MALE);
        p.setEmail("test@uv.mx");
        p.setEnrollment("zs20000000");
        assertDoesNotThrow(() -> UserValidator.validatePractitioner(p));
    }

    @Test
    void validatePractitioner_InvalidEnrollment_ThrowsManagerException() {
        Practitioner p = new Practitioner();
        p.setName("Angel");
        p.setLastName("Aguilar");
        p.setPassword("Password123");
        p.setGender(Gender.MALE);
        p.setEmail("test@uv.mx");
        p.setEnrollment("123");
        assertThrows(ManagerException.class, () -> UserValidator.validatePractitioner(p));
    }
}