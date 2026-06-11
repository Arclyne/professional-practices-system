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

    private static final int STORED_PROFESSOR_ID = 68;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ProfessorManager professorManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewProfessor_ValidData_ReturnsPassword() {
        Professor newProfessor = new Professor();
        newProfessor.setName("Norma Angelica");
        newProfessor.setLastName("Sandoval Rivas");
        newProfessor.setUserName("30036677");
        newProfessor.setEmail("nsandoval@uv.mx");
        newProfessor.setGender(Gender.FEMALE);

        assertDoesNotThrow(() -> professorManager.registerNewProfessor(newProfessor));
    }

    @Test
    void getAllProfessors_ReturnsExpectedList() throws ManagerException {
        List<Professor> expectedProfessors = new ArrayList<>();
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
        expectedProfessors.add(storedProfessor);

        List<Professor> resultProfessors = professorManager.getAllProfessors();

        assertEquals(expectedProfessors, resultProfessors);
    }

    @Test
    void inactivateMultipleProfessors_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> professorManager.inactivateMultipleProfessors(List.of(STORED_PROFESSOR_ID)));
    }
}
