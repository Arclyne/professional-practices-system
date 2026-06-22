package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PractitionerManagerTest {

    private static final String COORDINATOR_USERNAME = "mrodriguez";
    private static final int STORED_PRACTITIONER_ID = 123;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private PractitionerManager practitionerManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewPractitioner_ValidData_ReturnsPassword() {
        Practitioner newPractitioner = new Practitioner();
        newPractitioner.setName("Luis Fernando");
        newPractitioner.setLastName("Martinez Rivera");
        newPractitioner.setEnrollment("s25080910");
        newPractitioner.setEmail("zS25080910@estudiantes.uv.mx");
        newPractitioner.setGender(Gender.MALE);
        newPractitioner.setIndigenousLanguage("Ninguna");
        newPractitioner.setGrade(9.0);

        assertDoesNotThrow(() -> practitionerManager.registerNewPractitioner(newPractitioner));
    }

    @Test
    void retrievePractitionersPendingAssignment_ReturnsExpectedList() throws ManagerException {
        List<Practitioner> expectedPractitioners = new ArrayList<>();

        List<Practitioner> resultPractitioners = practitionerManager.retrievePractitionersPendingAssignment();

        assertEquals(expectedPractitioners, resultPractitioners);
    }

    @Test
    void retrieveAssignedPractitioners_ReturnsExpectedList() throws ManagerException {
        List<Practitioner> expectedPractitioners = new ArrayList<>();
        Practitioner assignedPractitioner = new Practitioner();
        assignedPractitioner.setId(123);
        assignedPractitioner.setUserName("zS24242424");
        assignedPractitioner.setEnrollment("zS24242424");
        assignedPractitioner.setName("Angel Gabriel");
        assignedPractitioner.setLastName("Aguilar Hernandez");
        assignedPractitioner.setEmail("zS24242424@estudiantes.uv.mx");
        expectedPractitioners.add(assignedPractitioner);

        List<Practitioner> resultPractitioners = practitionerManager.retrieveAssignedPractitioners();

        assertEquals(expectedPractitioners, resultPractitioners);
    }

    @Test
    void registerPractitionerBatch_ValidFile_ReturnsSummary() throws IOException, ManagerException {
        File batchCsvFile = Files.createTempFile("registro_practicantes", ".csv").toFile();
        Files.writeString(batchCsvFile.toPath(),
                "matricula,nombre,apellidos,correo,genero,lengua_indigena\n"
                        + "S20011111,Ana,Lopez Jimenez,zS20011111@estudiantes.uv.mx,Female,Nahuatl");
        batchCsvFile.deleteOnExit();
        BatchRegistrationSummary expectedSummary = new BatchRegistrationSummary();
        expectedSummary.incrementSuccess();

        BatchRegistrationSummary resultSummary = practitionerManager.registerPractitionerBatch(batchCsvFile, COORDINATOR_USERNAME);

        assertEquals(expectedSummary, resultSummary);
    }

    @Test
    void getPractitionerById_StoredId_ReturnsMatchingPractitioner() throws ManagerException {
        Practitioner resultPractitioner = practitionerManager.getPractitionerById(STORED_PRACTITIONER_ID);

        assertEquals(STORED_PRACTITIONER_ID, resultPractitioner.getId());
    }

    @Test
    void updatePractitioner_ValidPersonalData_DoesNotThrow() throws ManagerException {
        Practitioner practitionerToUpdate = practitionerManager.getPractitionerById(STORED_PRACTITIONER_ID);
        practitionerToUpdate.setName("Angel Gabriel Editado");

        assertDoesNotThrow(() -> practitionerManager.updatePractitioner(practitionerToUpdate, STORED_PRACTITIONER_ID));
    }

    @Test
    void registerPractitionerBatch_InvalidRow_CountsRowAsFailure() throws IOException, ManagerException {
        File batchCsvFile = Files.createTempFile("registro_practicantes", ".csv").toFile();
        Files.writeString(batchCsvFile.toPath(),
                "matricula,nombre,apellidos,correo,genero,lengua_indigena\n"
                        + "S20011111,Ana,Lopez Jimenez,zS20011111@estudiantes.uv.mx,Female,Nahuatl\n"
                        + "S20022222,Beto,Ramirez Soto,correo-sin-arroba,Male,Ninguna");
        batchCsvFile.deleteOnExit();
        BatchRegistrationSummary expectedSummary = new BatchRegistrationSummary();
        expectedSummary.incrementSuccess();
        expectedSummary.incrementFailure();

        BatchRegistrationSummary resultSummary = practitionerManager.registerPractitionerBatch(batchCsvFile, COORDINATOR_USERNAME);

        assertEquals(expectedSummary, resultSummary);
    }
}
