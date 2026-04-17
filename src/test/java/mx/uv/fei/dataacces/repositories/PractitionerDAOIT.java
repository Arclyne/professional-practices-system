package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import mx.uv.fei.TestApp;
import mx.uv.fei.TestConfig;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IPractitionerDAO;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class PractitionerDAOIT {

    @Autowired
    private IPractitionerDAO practitionerDAOTest;

    private Practitioner testPractitioner;

    @BeforeEach
    void setUp() {
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
    void testInsertPractitionerSuccess() {
        try {
            int resultId = practitionerDAOTest.insertPractitioner(testPractitioner);
            assertTrue(resultId > 0, "El practicante debió registrarse exitosamente en ambas tablas y devolver un ID mayor a 0");
        } catch (DAOException e) {
            fail("Falló la inserción del practicante: " + e.getMessage());
        }
    }

    @Test
    void testRecoverPractitionerSuccess() {
        try {
            int generatedId = practitionerDAOTest.insertPractitioner(testPractitioner);
            
            Practitioner recovered = practitionerDAOTest.recoverPractitioner(generatedId);

            assertNotNull(recovered, "El objeto recuperado no debería ser nulo");
            assertEquals("Angel Gabriel", recovered.getName(), "El nombre debería coincidir");
            assertEquals("Náhuatl", recovered.getIndigenousLanguage(), "La lengua debería coincidir");
            
        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testGetAllPractitionersSuccess() {
        try {
            practitionerDAOTest.insertPractitioner(testPractitioner);
            
            List<Practitioner> list = practitionerDAOTest.getAllPractitioners();
            
            assertTrue(list.size() > 0, "La lista debe contener al menos al practicante que acabamos de insertar");
        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testUpdatePractitionerSuccess() {
        try {
            int generatedId = practitionerDAOTest.insertPractitioner(testPractitioner);

            testPractitioner.setGrade(10.0);
            testPractitioner.setIndigenousLanguage("Maya");
            testPractitioner.setStatus("No Activo");

            boolean isUpdated = practitionerDAOTest.updatePractitioner(testPractitioner, generatedId);
            assertTrue(isUpdated, "La actualización en ambas tablas debió devolver true");

            Practitioner recovered = practitionerDAOTest.recoverPractitioner(generatedId);
            assertEquals(10.0, recovered.getGrade(), "La calificación debió actualizarse a 10.0");
            assertEquals("Maya", recovered.getIndigenousLanguage(), "La lengua debió actualizarse a Maya");
            assertEquals("No Activo", recovered.getStatus(), "El estado del usuario debió actualizarse");

        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }
}