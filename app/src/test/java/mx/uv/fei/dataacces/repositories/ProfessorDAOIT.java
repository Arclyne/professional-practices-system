package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProfessorDAO;

@StartEtiquetteTest
@Profile("test")
public class ProfessorDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IProfessorDAO professorDAOTest;

    private Professor testProfessor;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection);
        assertNotNull(professorDAOTest);

        TestDatabaseSetup.initialize(dbConnection);

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
        assertTrue(resultId > 0);
    }

    @Test
    void testRecoverProfessorSuccess() throws DAOException {
        int generatedId = professorDAOTest.insertProfessor(testProfessor);
        Professor recovered = professorDAOTest.recoverProfessor(generatedId);
        assertEquals(testProfessor, recovered);
    }

    @Test
    void testGetAllProfessorsSuccess() throws DAOException {
        professorDAOTest.insertProfessor(testProfessor);
        List<Professor> list = professorDAOTest.getAllProfessors();
        assertFalse(list.isEmpty());
    }

    @Test
    void testUpdateProfessorSuccess() throws DAOException {
        int generatedId = professorDAOTest.insertProfessor(testProfessor);

        testProfessor.setName("Angel Gabriel");
        testProfessor.setStatus("No Activo");

        professorDAOTest.updateProfessor(testProfessor, generatedId);

        Professor recovered = professorDAOTest.recoverProfessor(generatedId);
        assertEquals(testProfessor, recovered);
    }
}