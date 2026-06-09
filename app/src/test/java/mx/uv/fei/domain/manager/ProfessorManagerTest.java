package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ProfessorManagerTest {

    @Inject private IDatabaseConnection dbConnection;
    @Inject private ProfessorManager professorManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewProfessor_ValidData_ReturnsPassword() {
        Professor p = new Professor();
        p.setName("Prof");
        p.setLastName("Test");
        p.setUserName("111222");
        p.setEmail("prof@uv.mx");
        p.setGender(Gender.MALE);
        assertDoesNotThrow(() -> professorManager.registerNewProfessor(p));
    }

    @Test
    void getAllProfessors_ReturnsExpectedList() throws ManagerException {
        List<Professor> expectedList = new ArrayList<>();
        Professor prof = new Professor();
        prof.setId(68);
        prof.setUserName("prof1");
        prof.setPassword("12345");
        prof.setName("Prof");
        prof.setLastName("Test");
        prof.setEmail("prof1@uv.mx");
        prof.setRole("Professor");
        prof.setStatus(UserStatus.ACTIVE);
        prof.setGender(Gender.MALE);
        expectedList.add(prof);

        List<Professor> resultList = professorManager.getAllProfessors();
        assertEquals(expectedList, resultList);
    }

    @Test
    void inactivateMultipleProfessors_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> professorManager.inactivateMultipleProfessors(java.util.List.of(68)));
    }
}