package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
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
        int generatedId = professorDAO.insertProfessor(testProfessor);

        Professor recovered = professorDAO.recoverProfessor(generatedId);

        assertEquals(testProfessor, recovered);
    }

    @Test
    void getAllProfessors_WithExistingData_ReturnsList() throws DAOException {
        professorDAO.insertProfessor(testProfessor);

        List<Professor> resultList = professorDAO.getAllProfessors();

        assertFalse(resultList.isEmpty());
    }

    @Test
    void updateProfessor_ValidModifiedData_ReturnsUpdatedProfessor() throws DAOException {
        int generatedId = professorDAO.insertProfessor(testProfessor);
        testProfessor.setName("Angel Gabriel");
        testProfessor.setStatus(UserStatus.INACTIVE);

        professorDAO.updateProfessor(testProfessor, generatedId);
        Professor recovered = professorDAO.recoverProfessor(generatedId);

        assertEquals(testProfessor, recovered);
    }
}