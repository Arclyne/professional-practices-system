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
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IProfessorDAO;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class ProfessorDAOIT {

    @Autowired
    private IProfessorDAO professorDAOTest;

    private Professor testProfessor;

    @BeforeEach
    void setUp() {
        testProfessor = new Professor();
        
        testProfessor.setName("Angel");
        testProfessor.setLastName("Aguilar");
        testProfessor.setPassword("profesorPass123");
        testProfessor.setStatus("Activo");
        testProfessor.setGender("Masculino");
    }

    @Test
    void testInsertProfessorSuccess() {
        try {
            int resultId = professorDAOTest.insertProfessor(testProfessor);
            assertTrue(resultId > 0, "El profesor debió registrarse exitosamente en ambas tablas y devolver un ID mayor a 0");
        } catch (DAOException e) {
            fail("Falló la inserción del profesor: " + e.getMessage());
        }
    }

    @Test
    void testRecoverProfessorSuccess() {
        try {
            int generatedId = professorDAOTest.insertProfessor(testProfessor);
            
            Professor recovered = professorDAOTest.recoverProfessor(generatedId);

            assertNotNull(recovered, "El objeto recuperado no debería ser nulo");
            assertEquals("Angel", recovered.getName(), "El nombre debería coincidir");
            assertEquals("Aguilar", recovered.getLastName(), "Los apellidos deberían coincidir");
            assertNotNull(recovered.getRegistrationDate(), "La fecha de registro debió generarse automáticamente en la BD");
            
        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testGetAllProfessorsSuccess() {
        try {
            professorDAOTest.insertProfessor(testProfessor);
            
            List<Professor> list = professorDAOTest.getAllProfessors();
            
            assertTrue(list.size() > 0, "La lista debe contener al menos al profesor que acabamos de insertar");
        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testUpdateProfessorSuccess() {
        try {
            int generatedId = professorDAOTest.insertProfessor(testProfessor);
            testProfessor.setName("Angel Gabriel");
            testProfessor.setStatus("No Activo");

            boolean isUpdated = professorDAOTest.updateProfessor(testProfessor, generatedId);
            assertTrue(isUpdated, "La actualización en la tabla USUARIO debió devolver true");

            Professor recovered = professorDAOTest.recoverProfessor(generatedId);
            assertEquals("Angel Gabriel", recovered.getName(), "El nombre debió actualizarse");
            assertEquals("No Activo", recovered.getStatus(), "El estado debió actualizarse a No Activo");

        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }
}