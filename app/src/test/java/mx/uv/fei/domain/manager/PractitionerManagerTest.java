package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.sql.SQLException;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.exceptions.ManagerException;
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

        assertDoesNotThrow(() -> practitionerManager.registerNewPractitioner(p));
    }

    @Test
    void retrievePractitionersPendingAssignment_ReturnsList() throws ManagerException {
        assertNotNull(practitionerManager.retrievePractitionersPendingAssignment());
    }

    @Test
    void retrieveAssignedPractitioners_ReturnsList() throws ManagerException {
        assertNotNull(practitionerManager.retrieveAssignedPractitioners());
    }

    @Test
    void registerPractitionerBatch_ValidFile_ReturnsSummary() throws Exception {
        java.io.File tempCsv = java.nio.file.Files.createTempFile("batch", ".csv").toFile();
        java.nio.file.Files.writeString(tempCsv.toPath(), "matricula,nombre,apellidos,correo,genero,lengua_indigena\nS20011111,Ana,Lopez,alopez@uv.mx,Female,Nahuatl");
        tempCsv.deleteOnExit();

        assertNotNull(practitionerManager.registerPractitionerBatch(tempCsv, "coord_test"));
    }
}
