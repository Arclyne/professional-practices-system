package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

    private static final int STORED_PROFESSOR_ID = 68;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IProfessorDAO professorDAO;

    private Professor newProfessor;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newProfessor = new Professor();
        newProfessor.setName("Norma Angelica");
        newProfessor.setLastName("Sandoval Rivas");
        newProfessor.setUserName("nsandoval");
        newProfessor.setEmail("nsandoval@uv.mx");
        newProfessor.setPassword("ProfesoraUv2026");
        newProfessor.setRole("Professor");
        newProfessor.setStatus(UserStatus.ACTIVE);
        newProfessor.setGender(Gender.FEMALE);
    }

    private Professor buildStoredProfessor() {
        Professor storedProfessor = new Professor();
        storedProfessor.setId(STORED_PROFESSOR_ID);
        storedProfessor.setUserName("30033333");
        storedProfessor.setPassword("ProfeFei2026");
        storedProfessor.setName("Jose Eduardo");
        storedProfessor.setLastName("Prior Hernandez");
        storedProfessor.setEmail("eprior@uv.mx");
        storedProfessor.setRole("Professor");
        storedProfessor.setStatus(UserStatus.ACTIVE);
        storedProfessor.setGender(Gender.MALE);
        return storedProfessor;
    }

    @Test
    void insertProfessor_ValidProfessor_ReturnsGeneratedId() throws DAOException {
        int resultId = professorDAO.insertProfessor(newProfessor);

        assertTrue(resultId > 0);
    }

    @Test
    void recoverProfessor_ExistingId_ReturnsProfessor() throws DAOException {
        Professor expectedProfessor = buildStoredProfessor();

        Professor recoveredProfessor = professorDAO.recoverProfessor(STORED_PROFESSOR_ID);

        assertEquals(expectedProfessor, recoveredProfessor);
    }

    @Test
    void getAllProfessors_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Professor> expectedProfessors = new ArrayList<>();
        expectedProfessors.add(buildStoredProfessor());

        List<Professor> resultProfessors = professorDAO.getAllProfessors();

        assertEquals(expectedProfessors, resultProfessors);
    }

    @Test
    void updateProfessor_ValidModifiedData_DoesNotThrow() throws DAOException {
        newProfessor.setName("Norma Leticia");
        newProfessor.setStatus(UserStatus.INACTIVE);

        assertDoesNotThrow(() -> professorDAO.updateProfessor(newProfessor, STORED_PROFESSOR_ID));
    }

    @Test
    void insertProfessor_DuplicateUsername_ThrowsDAOException() {
        newProfessor.setUserName("30033333");

        assertThrows(DAOException.class, () -> professorDAO.insertProfessor(newProfessor));
    }

    @Test
    void recoverProfessor_NonExistentId_ReturnsEmptyProfessor() throws DAOException {
        Professor recoveredProfessor = professorDAO.recoverProfessor(NON_EXISTENT_ID);

        assertEquals(new Professor(), recoveredProfessor);
    }

    @Test
    void updateProfessor_NonExistentId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> professorDAO.updateProfessor(newProfessor, NON_EXISTENT_ID));
    }
}
