package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.sql.SQLException;

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
public class PractitionerDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPractitionerDAO practitionerDAOTest;

    private Practitioner testPractitioner;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection);
        assertNotNull(practitionerDAOTest);

        TestDatabaseSetup.initialize(dbConnection);

        testPractitioner = new Practitioner();
        testPractitioner.setName("Angel Gabriel");
        testPractitioner.setLastName("Aguilar Hernandez");
        testPractitioner.setPassword("practicantePass123");
        testPractitioner.setStatus("Activo");
        testPractitioner.setGender("Masculino");
        testPractitioner.setIndigenousLanguage("Náhuatl");
        testPractitioner.setGrade(9.5);
    }

    @Test
    void testInsertPractitionerSuccess() throws DAOException {
        int resultId = practitionerDAOTest.insertPractitioner(testPractitioner);
        assertTrue(resultId > 0);
    }

    @Test
    void testRecoverPractitionerSuccess() throws DAOException {
        int generatedId = practitionerDAOTest.insertPractitioner(testPractitioner);
        Practitioner recovered = practitionerDAOTest.recoverPractitioner(generatedId);
        assertEquals(testPractitioner, recovered);
    }

    @Test
    void testGetAllPractitionersSuccess() throws DAOException {
        practitionerDAOTest.insertPractitioner(testPractitioner);
        List<Practitioner> list = practitionerDAOTest.getAllPractitioners();
        assertFalse(list.isEmpty());
    }

    @Test
    void testUpdatePractitionerSuccess() throws DAOException {
        int generatedId = practitionerDAOTest.insertPractitioner(testPractitioner);

        testPractitioner.setGrade(10.0);
        testPractitioner.setIndigenousLanguage("Maya");
        testPractitioner.setStatus("No Activo");

        practitionerDAOTest.updatePractitioner(testPractitioner, generatedId);

        Practitioner recovered = practitionerDAOTest.recoverPractitioner(generatedId);
        assertEquals(testPractitioner, recovered);
    }
}