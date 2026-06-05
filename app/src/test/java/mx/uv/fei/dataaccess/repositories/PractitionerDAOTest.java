package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.sql.SQLException;

import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;

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
        assertNotNull(dbConnection);
        assertNotNull(practitionerDAO);
        TestDatabaseSetup.initialize(dbConnection);
        testPractitioner = new Practitioner();
        testPractitioner.setName("Angel Gabriel");
        testPractitioner.setLastName("Aguilar Hernandez");
        testPractitioner.setPassword("practicantePass123");
        testPractitioner.setStatus(UserStatus.valueOf("Activo"));
        testPractitioner.setGender(Gender.valueOf("Masculino"));
        testPractitioner.setIndigenousLanguage("Náhuatl");
        testPractitioner.setGrade(9.5);
    }

    @Test
    void insertPractitioner_ValidPractitioner_ReturnsGeneratedId() throws DAOException {

        int resultId = practitionerDAO.insertPractitioner(testPractitioner);

        assertTrue(resultId > 0);
    }

    @Test
    void recoverPractitioner_ExistingId_ReturnsPractitioner() throws DAOException {
        int generatedId = practitionerDAO.insertPractitioner(testPractitioner);

        Practitioner recovered = practitionerDAO.recoverPractitioner(generatedId);

        assertEquals(testPractitioner, recovered);
    }

    @Test
    void getAllPractitioners_WithExistingData_ReturnsList() throws DAOException {
        practitionerDAO.insertPractitioner(testPractitioner);

        List<Practitioner> list = practitionerDAO.getAllPractitioners();

        assertFalse(list.isEmpty());
    }

    @Test
    void updatePractitioner_ValidModifiedData_ReturnsUpdatedPractitioner() throws DAOException {
        int generatedId = practitionerDAO.insertPractitioner(testPractitioner);
        testPractitioner.setGrade(10.0);
        testPractitioner.setIndigenousLanguage("Maya");
        testPractitioner.setStatus(UserStatus.valueOf("No Activo"));

        practitionerDAO.updatePractitioner(testPractitioner, generatedId);
        Practitioner recovered = practitionerDAO.recoverPractitioner(generatedId);

        assertEquals(testPractitioner, recovered);
    }
}