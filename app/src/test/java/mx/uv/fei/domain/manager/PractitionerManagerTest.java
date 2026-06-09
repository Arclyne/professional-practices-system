package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.BatchRegistrationSummary;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
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
    void retrievePractitionersPendingAssignment_ReturnsExpectedList() throws ManagerException {
        List<Practitioner> expectedList = new ArrayList<>();
        List<Practitioner> resultList = practitionerManager.retrievePractitionersPendingAssignment();

        assertEquals(expectedList, resultList);
    }

    @Test
    void retrieveAssignedPractitioners_ReturnsExpectedList() throws ManagerException {
        List<Practitioner> expectedList = new ArrayList<>();
        Practitioner p = new Practitioner();
        p.setId(123);
        p.setUserName("zS24242424");
        p.setEnrollment("zS24242424");
        p.setName("Angel");
        p.setLastName("Aguilar");
        p.setEmail("angel24@gmail.com");
        expectedList.add(p);

        List<Practitioner> resultList = practitionerManager.retrieveAssignedPractitioners();
        assertEquals(expectedList, resultList);
    }

    @Test
    void registerPractitionerBatch_ValidFile_ReturnsSummary() throws Exception {
        java.io.File tempCsv = java.nio.file.Files.createTempFile("batch", ".csv").toFile();
        java.nio.file.Files.writeString(tempCsv.toPath(), "matricula,nombre,apellidos,correo,genero,lengua_indigena\nS20011111,Ana,Lopez,alopez@uv.mx,Female,Nahuatl");
        tempCsv.deleteOnExit();

        BatchRegistrationSummary expectedSummary = new BatchRegistrationSummary();
        expectedSummary.incrementSuccess();

        BatchRegistrationSummary actualSummary = practitionerManager.registerPractitionerBatch(tempCsv, "coord_test");
        assertEquals(expectedSummary, actualSummary);
    }
}