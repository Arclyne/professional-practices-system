package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

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
    void testInsertProfessorSuccess() {
        try {
            int resultId = professorDAOTest.insertProfessor(testProfessor);
            assertTrue(resultId > 0,
                    "El profesor debió registrarse exitosamente en ambas tablas y devolver un ID mayor a 0");
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
            assertNotNull(recovered.getRegistrationDate(),
                    "La fecha de registro debió generarse automáticamente en la BD");

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