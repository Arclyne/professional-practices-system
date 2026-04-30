package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IProfessorDAO;

public class ProfessorDAOIT {

    private IDatabaseConnection dbConnection;
    private DatabasePropeties propeties;
    private UserDAO userDAOTest;

    private IProfessorDAO professorDAOTest;
    private Professor testProfessor;

    @BeforeEach
    void setUp() throws SQLException {
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        TestDatabaseSetup.initialize(dbConnection);
        userDAOTest = new UserDAO(dbConnection);

        professorDAOTest = new ProfessorDAO(dbConnection, userDAOTest);
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