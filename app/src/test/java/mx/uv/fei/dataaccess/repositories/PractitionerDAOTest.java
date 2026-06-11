package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PractitionerDAOTest {

    private static final int STORED_PRACTITIONER_ID = 123;
    private static final int STORED_GROUP_ID = 6;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPractitionerDAO practitionerDAO;

    private Practitioner newPractitioner;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newPractitioner = new Practitioner();
        newPractitioner.setName("Luis Fernando");
        newPractitioner.setLastName("Martinez Rivera");
        newPractitioner.setUserName("zS25080910");
        newPractitioner.setEmail("zS25080910@estudiantes.uv.mx");
        newPractitioner.setPassword("PracticanteUv2026");
        newPractitioner.setRole("Practitioner");
        newPractitioner.setStatus(UserStatus.ACTIVE);
        newPractitioner.setGender(Gender.MALE);
        newPractitioner.setIndigenousLanguage("Nahuatl");
        newPractitioner.setGrade(9.5);
        newPractitioner.setGroupId(STORED_GROUP_ID);
    }

    private Practitioner buildStoredPractitioner() {
        Practitioner storedPractitioner = new Practitioner();
        storedPractitioner.setId(STORED_PRACTITIONER_ID);
        storedPractitioner.setUserName("zS24242424");
        storedPractitioner.setEnrollment("zS24242424");
        storedPractitioner.setPassword("PracticasUv2026");
        storedPractitioner.setName("Angel Gabriel");
        storedPractitioner.setLastName("Aguilar Hernandez");
        storedPractitioner.setEmail("zS24242424@estudiantes.uv.mx");
        storedPractitioner.setStatus(UserStatus.ACTIVE);
        storedPractitioner.setGender(Gender.MALE);
        storedPractitioner.setRole("Practitioner");
        storedPractitioner.setIndigenousLanguage("Ninguna");
        storedPractitioner.setGrade(0.00);
        storedPractitioner.setGroupId(STORED_GROUP_ID);
        return storedPractitioner;
    }

    @Test
    void insertPractitioner_ValidPractitioner_ReturnsGeneratedId() throws DAOException {
        int resultId = practitionerDAO.insertPractitioner(newPractitioner);

        assertTrue(resultId > 0);
    }

    @Test
    void recoverPractitioner_ExistingId_ReturnsPractitioner() throws DAOException {
        Practitioner expectedPractitioner = buildStoredPractitioner();

        Practitioner recoveredPractitioner = practitionerDAO.recoverPractitioner(STORED_PRACTITIONER_ID);

        assertEquals(expectedPractitioner, recoveredPractitioner);
    }

    @Test
    void getAllPractitioners_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Practitioner> expectedPractitioners = new ArrayList<>();
        expectedPractitioners.add(buildStoredPractitioner());

        List<Practitioner> resultPractitioners = practitionerDAO.getAllPractitioners();

        assertEquals(expectedPractitioners, resultPractitioners);
    }

    @Test
    void updatePractitioner_ValidModifiedData_ReturnsTrue() throws DAOException {
        newPractitioner.setGrade(10.0);
        newPractitioner.setIndigenousLanguage("Maya");
        newPractitioner.setStatus(UserStatus.INACTIVE);

        boolean isUpdated = practitionerDAO.updatePractitioner(newPractitioner, STORED_PRACTITIONER_ID);

        assertTrue(isUpdated);
    }

    @Test
    void retrievePractitionersPendingAssignment_WithExistingData_ReturnsEmptyList() throws DAOException {
        List<Practitioner> expectedPractitioners = new ArrayList<>();

        List<Practitioner> resultPractitioners = practitionerDAO.retrievePractitionersPendingAssignment();

        assertEquals(expectedPractitioners, resultPractitioners);
    }

    @Test
    void retrieveAssignedPractitioners_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Practitioner> expectedPractitioners = new ArrayList<>();
        Practitioner assignedPractitioner = new Practitioner();
        assignedPractitioner.setId(STORED_PRACTITIONER_ID);
        assignedPractitioner.setUserName("zS24242424");
        assignedPractitioner.setEnrollment("zS24242424");
        assignedPractitioner.setName("Angel Gabriel");
        assignedPractitioner.setLastName("Aguilar Hernandez");
        assignedPractitioner.setEmail("zS24242424@estudiantes.uv.mx");
        expectedPractitioners.add(assignedPractitioner);

        List<Practitioner> resultPractitioners = practitionerDAO.retrieveAssignedPractitioners();

        assertEquals(expectedPractitioners, resultPractitioners);
    }

    @Test
    void insertPractitioner_DuplicateEmail_ThrowsDAOException() {
        newPractitioner.setEmail("zS24242424@estudiantes.uv.mx");

        assertThrows(DAOException.class, () -> practitionerDAO.insertPractitioner(newPractitioner));
    }

    @Test
    void recoverPractitioner_NonExistentId_ReturnsEmptyPractitioner() throws DAOException {
        Practitioner recoveredPractitioner = practitionerDAO.recoverPractitioner(NON_EXISTENT_ID);

        assertEquals(new Practitioner(), recoveredPractitioner);
    }

    @Test
    void updatePractitioner_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = practitionerDAO.updatePractitioner(newPractitioner, NON_EXISTENT_ID);

        assertFalse(isUpdated);
    }
}
