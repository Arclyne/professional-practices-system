package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProfessorDAO;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ProfessorDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IProfessorDAO professorDAO;

    private Professor testProfessor;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testProfessor = new Professor();
        testProfessor.setName("Angel");
        testProfessor.setLastName("Aguilar");
        testProfessor.setUserName("profAguilar");
        testProfessor.setEmail("profAguilar@uv.mx");
        testProfessor.setPassword("profesorPass123");
        testProfessor.setRole("Professor");
        testProfessor.setStatus(UserStatus.ACTIVE);
        testProfessor.setGender(Gender.MALE);
    }

    @Test
    void insertProfessor_ValidProfessor_ReturnsGeneratedId() throws DAOException {
        int resultId = professorDAO.insertProfessor(testProfessor);
        assertTrue(resultId > 0);
    }

    @Test
    void recoverProfessor_ExistingId_ReturnsProfessor() throws DAOException {
        Professor expectedProfessor = new Professor();
        expectedProfessor.setId(68);
        expectedProfessor.setUserName("prof1");
        expectedProfessor.setPassword("12345");
        expectedProfessor.setName("Prof");
        expectedProfessor.setLastName("Test");
        expectedProfessor.setEmail("prof1@uv.mx");
        expectedProfessor.setRole("Professor");
        expectedProfessor.setStatus(UserStatus.ACTIVE);
        expectedProfessor.setGender(Gender.MALE);

        Professor recovered = professorDAO.recoverProfessor(68);
        assertEquals(expectedProfessor, recovered);
    }

    @Test
    void getAllProfessors_WithExistingData_ReturnsExpectedList() throws DAOException {
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

        List<Professor> resultList = professorDAO.getAllProfessors();
        assertEquals(expectedList, resultList);
    }

    @Test
    void updateProfessor_ValidModifiedData_ReturnsTrue() throws DAOException {
        testProfessor.setName("Angel Gabriel");
        testProfessor.setStatus(UserStatus.INACTIVE);

        boolean isUpdated = professorDAO.updateProfessor(testProfessor, 68);
        assertTrue(isUpdated);
    }

    @Test
    void insertProfessor_DuplicateUsername_ThrowsDAOException() {
        testProfessor.setUserName("prof1");
        assertThrows(DAOException.class, () -> professorDAO.insertProfessor(testProfessor));
    }

    @Test
    void recoverProfessor_NonExistentId_ReturnsEmptyProfessor() throws DAOException {
        Professor recovered = professorDAO.recoverProfessor(9999);
        assertEquals(new Professor(), recovered);
    }

    @Test
    void updateProfessor_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = professorDAO.updateProfessor(testProfessor, 9999);
        assertFalse(isUpdated);
    }
}