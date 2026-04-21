package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IPractitionerDAO;

public class PractitionerDAOIT {

    private IDatabaseConnection dbConnection;
    private DatabasePropeties propeties;

    private IPractitionerDAO practitionerDAOTest;
    private Practitioner testPractitioner;
    private UserDAO userDAOTest;

    @BeforeEach
    void setUp() throws SQLException {
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        TestDatabaseSetup.initialize(dbConnection);
        userDAOTest = new UserDAO(dbConnection);
        practitionerDAOTest = new PractitionerDAO(dbConnection, userDAOTest);
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

        assertTrue(resultId > 0, "El practicante debió registrarse exitosamente y devolver un ID mayor a 0");
    }

    @Test
    void testRecoverPractitionerSuccess() throws DAOException {
        int generatedId = practitionerDAOTest.insertPractitioner(testPractitioner);

        Practitioner recovered = practitionerDAOTest.recoverPractitioner(generatedId);

        assertEquals(testPractitioner, recovered, "El practicante recuperado no coincide con el insertado.");
    }

    @Test
    void testGetAllPractitionersSuccess() throws DAOException {
        practitionerDAOTest.insertPractitioner(testPractitioner);

        List<Practitioner> list = practitionerDAOTest.getAllPractitioners();

        assertFalse(list.isEmpty(), "La lista debe contener al menos al practicante que acabamos de insertar");
    }

    @Test
    void testUpdatePractitionerSuccess() throws DAOException {
        int generatedId = practitionerDAOTest.insertPractitioner(testPractitioner);

        testPractitioner.setGrade(10.0);
        testPractitioner.setIndigenousLanguage("Maya");
        testPractitioner.setStatus("No Activo");

        practitionerDAOTest.updatePractitioner(testPractitioner, generatedId);

        Practitioner recovered = practitionerDAOTest.recoverPractitioner(generatedId);
        assertEquals(testPractitioner, recovered, "Los datos del practicante recuperado no reflejan la actualización.");
    }
}