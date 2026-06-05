package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
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
public class PracticeGroupDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPracticeGroupDAO groupDAO;

    @Inject
    private IProfessorDAO professorDAO;

    @Inject
    private ISchoolPeriodDAO periodDAO;

    private PracticeGroup testGroup;

    private int validProfessorId;
    private int validPeriodId;

    @BeforeEach
    void setUp() throws DAOException, SQLException {
        assertNotNull(dbConnection);
        assertNotNull(groupDAO);
        assertNotNull(professorDAO);
        assertNotNull(periodDAO);

        TestDatabaseSetup.initialize(dbConnection);

        Professor tempProf = new Professor();
        tempProf.setName("Profesor");
        tempProf.setLastName("Prueba Grupo");
        tempProf.setPassword("1234");
        tempProf.setStatus(UserStatus.valueOf("Activo"));
        tempProf.setGender(Gender.valueOf("Masculino"));
        validProfessorId = professorDAO.insertProfessor(tempProf);

        SchoolPeriod tempPeriod = new SchoolPeriod();
        tempPeriod.setPeriodName("Periodo Prueba Grupo");
        tempPeriod.setStartDate(LocalDate.of(2026, 8, 15));
        tempPeriod.setEndDate(LocalDate.of(2027, 1, 20));
        tempPeriod.setStatus("activo");
        validPeriodId = periodDAO.insertSchoolPeriod(tempPeriod);

        testGroup = new PracticeGroup();
        testGroup.setSection("NRC-84932");
        testGroup.setProfessorId(validProfessorId);
        testGroup.setPeriodId(validPeriodId);
    }

    @Test
    void insertPracticeGroup_ValidGroup_ReturnsGeneratedId() throws DAOException {
        int resultId = groupDAO.insertPracticeGroup(testGroup);

        assertTrue(resultId > 0, "El grupo de práctica debió insertarse y generar un ID mayor a cero.");
    }

    @Test
    void recoverPracticeGroup_ExistingId_ReturnsGroup() throws DAOException {

        int generatedId = groupDAO.insertPracticeGroup(testGroup);


        PracticeGroup recovered = groupDAO.recoverPracticeGroup(generatedId);


        assertEquals(testGroup, recovered, "El grupo recuperado debe ser idéntico al original.");
    }

    @Test
    void getAllPracticeGroups_WithExistingData_ReturnsList() throws DAOException {

        groupDAO.insertPracticeGroup(testGroup);


        List<PracticeGroup> list = groupDAO.getAllPracticeGroups();


        assertFalse(list.isEmpty(), "La lista recuperada no debe estar vacía.");
    }

    @Test
    void updatePracticeGroup_ValidModifiedData_ReturnsTrue() throws DAOException {

        int generatedId = groupDAO.insertPracticeGroup(testGroup);
        testGroup.setSection("NRC-99999");


        groupDAO.updatePracticeGroup(testGroup, generatedId);
        PracticeGroup recovered = groupDAO.recoverPracticeGroup(generatedId);


        assertEquals(testGroup, recovered, "La sección recuperada debe coincidir con la modificación.");
    }
}