package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IPracticeGroupDAO;
import mx.uv.fei.dataacces.interfaces.IProfessorDAO;
import mx.uv.fei.dataacces.interfaces.ISchoolPeriodDAO;

public class PracticeGroupDAOIT {

    private IDatabaseConnection dbConnection;
    private DatabasePropeties propeties;

    private UserDAO userDAOTest;
    private IPracticeGroupDAO groupDAOTest;
    private IProfessorDAO professorDAOTest;
    private ISchoolPeriodDAO periodDAOTest;

    private PracticeGroup testGroup;

    private int validProfessorId;
    private int validPeriodId;

    @BeforeEach
    void setUp() throws DAOException, SQLException {

        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        TestDatabaseSetup.initialize(dbConnection);
        userDAOTest = new UserDAO(dbConnection);
        groupDAOTest = new PracticeGroupDAO(dbConnection);
        professorDAOTest = new ProfessorDAO(dbConnection, userDAOTest);
        periodDAOTest = new SchoolPeriodDAO(dbConnection);

        Professor tempProf = new Professor();
        tempProf.setName("Profesor");
        tempProf.setLastName("Prueba Grupo");
        tempProf.setPassword("1234");
        tempProf.setStatus("Activo");
        tempProf.setGender("Masculino");
        validProfessorId = professorDAOTest.insertProfessor(tempProf);

        SchoolPeriod tempPeriod = new SchoolPeriod();
        tempPeriod.setPeriodName("Periodo Prueba Grupo");
        tempPeriod.setStartDate(LocalDate.of(2026, 8, 15));
        tempPeriod.setEndDate(LocalDate.of(2027, 1, 20));
        tempPeriod.setStatus("activo");
        validPeriodId = periodDAOTest.insertSchoolPeriod(tempPeriod);

        testGroup = new PracticeGroup();
        testGroup.setSection("NRC-84932");
        testGroup.setProfessorId(validProfessorId);
        testGroup.setPeriodId(validPeriodId);

    }

    @Test
    void testInsertPracticeGroupSuccess() {
        try {
            int resultId = groupDAOTest.insertPracticeGroup(testGroup);
            assertTrue(resultId > 0, "El grupo de prácticas debió registrarse exitosamente y devolver un ID mayor a 0");
        } catch (DAOException e) {
            String motivoReal = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            fail("Falló la inserción del grupo de prácticas: " + motivoReal);
        }
    }

    @Test
    void testRecoverPracticeGroupSuccess() {
        try {
            int generatedId = groupDAOTest.insertPracticeGroup(testGroup);

            PracticeGroup recovered = groupDAOTest.recoverPracticeGroup(generatedId);

            assertNotNull(recovered, "El objeto recuperado no debería ser nulo");
            assertEquals("NRC-84932", recovered.getSection(), "La sección debería coincidir");
            assertEquals(validProfessorId, recovered.getProfessorId(), "El ID del profesor debería coincidir");
            assertEquals(validPeriodId, recovered.getPeriodId(), "El ID del periodo debería coincidir");

        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testGetAllPracticeGroupsSuccess() {
        try {
            groupDAOTest.insertPracticeGroup(testGroup);

            List<PracticeGroup> list = groupDAOTest.getAllPracticeGroups();

            assertTrue(list.size() > 0,
                    "La lista debe contener al menos el grupo de prácticas que acabamos de insertar");
        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testUpdatePracticeGroupSuccess() {
        try {
            int generatedId = groupDAOTest.insertPracticeGroup(testGroup);

            testGroup.setSection("NRC-99999");

            boolean isUpdated = groupDAOTest.updatePracticeGroup(testGroup, generatedId);
            assertTrue(isUpdated, "La actualización debió devolver true");

            PracticeGroup recovered = groupDAOTest.recoverPracticeGroup(generatedId);
            assertEquals("NRC-99999", recovered.getSection(), "La sección debió actualizarse a NRC-99999");

        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }
}