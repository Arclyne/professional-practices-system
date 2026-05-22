package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPracticeGroupDAO;
import mx.uv.fei.dataaccess.interfaces.IProfessorDAO;
import mx.uv.fei.dataaccess.interfaces.ISchoolPeriodDAO;

@StartEtiquetteTest
@Profile("test")
public class PracticeGroupDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPracticeGroupDAO groupDAOTest;

    @Inject
    private IProfessorDAO professorDAOTest;

    @Inject
    private ISchoolPeriodDAO periodDAOTest;

    private PracticeGroup testGroup;

    private int validProfessorId;
    private int validPeriodId;

    @BeforeEach
    void setUp() throws DAOException, SQLException {
        assertNotNull(dbConnection);
        assertNotNull(groupDAOTest);
        assertNotNull(professorDAOTest);
        assertNotNull(periodDAOTest);

        TestDatabaseSetup.initialize(dbConnection);

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
        assertTrue(resultId > 0);
    }

    @Test
    void testRecoverPracticeGroupSuccess() throws DAOException {
        int generatedId = groupDAOTest.insertPracticeGroup(testGroup);
        PracticeGroup recovered = groupDAOTest.recoverPracticeGroup(generatedId);
        assertEquals(testGroup, recovered);
    }

    @Test
    void testGetAllPracticeGroupsSuccess() throws DAOException {
        groupDAOTest.insertPracticeGroup(testGroup);
        List<PracticeGroup> list = groupDAOTest.getAllPracticeGroups();
        assertFalse(list.isEmpty());
    }

    @Test
    void testUpdatePracticeGroupSuccess() throws DAOException {
        int generatedId = groupDAOTest.insertPracticeGroup(testGroup);
        testGroup.setSection("NRC-99999");
        groupDAOTest.updatePracticeGroup(testGroup, generatedId);
        PracticeGroup recovered = groupDAOTest.recoverPracticeGroup(generatedId);
        assertEquals(testGroup, recovered);
    }
}