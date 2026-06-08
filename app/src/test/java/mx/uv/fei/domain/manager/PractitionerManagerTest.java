package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PractitionerManagerTest {

    @Inject private IDatabaseConnection dbConnection;
    @Inject private PractitionerManager practitionerManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewPractitioner_ValidData_ReturnsPassword() {
        Practitioner p = new Practitioner();
        p.setName("New");
        p.setLastName("User");
        p.setEnrollment("s20000000");
        p.setEmail("new@uv.mx");
        p.setGender(Gender.MALE);
        p.setIndigenousLanguage("Ninguna");
        p.setGrade(9.0);

        assertNotNull(assertDoesNotThrow(() -> practitionerManager.registerNewPractitioner(p)));
    }
}