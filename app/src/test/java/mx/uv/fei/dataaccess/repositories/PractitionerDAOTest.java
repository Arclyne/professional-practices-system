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

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPractitionerDAO practitionerDAO;

    private Practitioner testPractitioner;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testPractitioner = new Practitioner();
        testPractitioner.setName("Angel Gabriel");
        testPractitioner.setLastName("Aguilar Hernandez");
        testPractitioner.setUserName("aguilar");
        testPractitioner.setEmail("aguilar@uv.mx");
        testPractitioner.setPassword("practicantePass123");
        testPractitioner.setRole("Practitioner");
        testPractitioner.setStatus(UserStatus.ACTIVE);
        testPractitioner.setGender(Gender.MALE);
        testPractitioner.setIndigenousLanguage("Nahuatl");
        testPractitioner.setGrade(9.5);
        testPractitioner.setGroupId(6);
    }

    @Test
    void insertPractitioner_ValidPractitioner_ReturnsGeneratedId() throws DAOException {
        int resultId = practitionerDAO.insertPractitioner(testPractitioner);
        assertTrue(resultId > 0);
    }

    @Test
    void recoverPractitioner_ExistingId_ReturnsPractitioner() throws DAOException {
        Practitioner expected = new Practitioner();
        expected.setId(123);
        expected.setUserName("zS24242424");
        expected.setEnrollment("zS24242424");
        expected.setPassword("12345");
        expected.setName("Angel");
        expected.setLastName("Aguilar");
        expected.setEmail("angel24@gmail.com");
        expected.setStatus(UserStatus.ACTIVE);
        expected.setGender(Gender.MALE);
        expected.setRole("Practitioner");
        expected.setIndigenousLanguage("Ninguna");
        expected.setGrade(0.00);
        expected.setGroupId(6);

        Practitioner recovered = practitionerDAO.recoverPractitioner(123);
        assertEquals(expected, recovered);
    }

    @Test
    void getAllPractitioners_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Practitioner> expectedList = new ArrayList<>();
        Practitioner p = new Practitioner();
        p.setId(123);
        p.setUserName("zS24242424");
        p.setEnrollment("zS24242424");
        p.setPassword("12345");
        p.setName("Angel");
        p.setLastName("Aguilar");
        p.setEmail("angel24@gmail.com");
        p.setStatus(UserStatus.ACTIVE);
        p.setGender(Gender.MALE);
        p.setRole("Practitioner");
        p.setIndigenousLanguage("Ninguna");
        p.setGrade(0.00);
        p.setGroupId(6);
        expectedList.add(p);

        List<Practitioner> resultList = practitionerDAO.getAllPractitioners();
        assertEquals(expectedList, resultList);
    }

    @Test
    void updatePractitioner_ValidModifiedData_ReturnsTrue() throws DAOException {
        testPractitioner.setGrade(10.0);
        testPractitioner.setIndigenousLanguage("Maya");
        testPractitioner.setStatus(UserStatus.INACTIVE);

        boolean isUpdated = practitionerDAO.updatePractitioner(testPractitioner, 123);
        assertTrue(isUpdated);
    }

    @Test
    void retrievePractitionersPendingAssignment_WithExistingData_ReturnsEmptyList() throws DAOException {
        List<Practitioner> expectedList = new ArrayList<>();
        List<Practitioner> resultList = practitionerDAO.retrievePractitionersPendingAssignment();
        assertEquals(expectedList, resultList);
    }

    @Test
    void retrieveAssignedPractitioners_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Practitioner> expectedList = new ArrayList<>();
        Practitioner p = new Practitioner();
        p.setId(123);
        p.setUserName("zS24242424");
        p.setEnrollment("zS24242424");
        p.setName("Angel");
        p.setLastName("Aguilar");
        p.setEmail("angel24@gmail.com");
        expectedList.add(p);

        List<Practitioner> resultList = practitionerDAO.retrieveAssignedPractitioners();
        assertEquals(expectedList, resultList);
    }

    @Test
    void insertPractitioner_DuplicateEmail_ThrowsDAOException() {
        testPractitioner.setEmail("angel24@gmail.com");
        assertThrows(DAOException.class, () -> practitionerDAO.insertPractitioner(testPractitioner));
    }

    @Test
    void recoverPractitioner_NonExistentId_ReturnsEmptyPractitioner() throws DAOException {
        Practitioner recovered = practitionerDAO.recoverPractitioner(9999);
        assertEquals(new Practitioner(), recovered);
    }

    @Test
    void updatePractitioner_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = practitionerDAO.updatePractitioner(testPractitioner, 9999);
        assertFalse(isUpdated);
    }
}