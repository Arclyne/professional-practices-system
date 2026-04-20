package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testInsertProfessorSuccess() throws DAOException {
        int resultId = professorDAOTest.insertProfessor(testProfessor);
        
        assertTrue(resultId > 0, "El profesor debió registrarse exitosamente y devolver un ID mayor a 0");
    }

    @Test
    void testRecoverProfessorSuccess() throws DAOException {
        int generatedId = professorDAOTest.insertProfessor(testProfessor);
        
        Professor recovered = professorDAOTest.recoverProfessor(generatedId);

        assertEquals(testProfessor, recovered, "El profesor recuperado no coincide con el insertado.");
    }

    @Test
    void testGetAllProfessorsSuccess() throws DAOException {
        professorDAOTest.insertProfessor(testProfessor);
        
        List<Professor> list = professorDAOTest.getAllProfessors();
        
        assertFalse(list.isEmpty(), "La lista debe contener al menos al profesor que acabamos de insertar");
    }

    @Test
    void testUpdateProfessorSuccess() throws DAOException {
        int generatedId = professorDAOTest.insertProfessor(testProfessor);
        
        testProfessor.setName("Angel Gabriel");
        testProfessor.setStatus("No Activo");

        professorDAOTest.updateProfessor(testProfessor, generatedId);

        Professor recovered = professorDAOTest.recoverProfessor(generatedId);
        assertEquals(testProfessor, recovered, "Los datos del profesor recuperado no reflejan la actualización.");
    }
}