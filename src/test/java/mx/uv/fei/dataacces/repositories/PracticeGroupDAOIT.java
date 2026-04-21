package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void testInsertPracticeGroupSuccess() throws DAOException {
        int resultId = groupDAOTest.insertPracticeGroup(testGroup);

        assertTrue(resultId > 0, "El grupo de prácticas debió registrarse exitosamente y devolver un ID mayor a 0");
    }

    @Test
    void testRecoverPracticeGroupSuccess() throws DAOException {
        int generatedId = groupDAOTest.insertPracticeGroup(testGroup);

        PracticeGroup recovered = groupDAOTest.recoverPracticeGroup(generatedId);

        assertEquals(testGroup, recovered, "El grupo de prácticas recuperado no coincide con el insertado.");
    }

    @Test
    void testGetAllPracticeGroupsSuccess() throws DAOException {
        groupDAOTest.insertPracticeGroup(testGroup);

        List<PracticeGroup> list = groupDAOTest.getAllPracticeGroups();

        assertFalse(list.isEmpty(), "La lista debe contener al menos el grupo de prácticas que acabamos de insertar");
    }

    @Test
    void testUpdatePracticeGroupSuccess() throws DAOException {
        int generatedId = groupDAOTest.insertPracticeGroup(testGroup);

        testGroup.setSection("NRC-99999");

        groupDAOTest.updatePracticeGroup(testGroup, generatedId);

        PracticeGroup recovered = groupDAOTest.recoverPracticeGroup(generatedId);
        assertEquals(testGroup, recovered, "Los datos del grupo de prácticas recuperado no reflejan la actualización.");
    }
}