package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.dataacces.exceptions.DAOException;

@SpringBootTest
@ActiveProfiles("test")
public class ProfessorDAOTest {

    @Autowired
    private ProfessorDAO professorDAO;
    @Autowired
    private Professor testProfessor;

    @BeforeEach
    void setUp() {

        testProfessor = new Professor();
        testProfessor.setName("Angel");
        testProfessor.setLastName("Aguilar");
        testProfessor.setPassword("profesorPass123");
        testProfessor.setStatus("activo");
        testProfessor.setGender("Masculino");
    }

    @Test
    void testInsertProfessorSuccess() {
        try {
            int resultId = professorDAO.insertProfessor(testProfessor);

            assertTrue(resultId > 0, "El profesor debió registrarse exitosamente y devolver un ID mayor a 0");

        } catch (DAOException e) {
            fail("Falló la inserción del profesor: " + e.getMessage());
        }
    }
}