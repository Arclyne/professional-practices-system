package mx.uv.fei.domain.manager.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        storedProfessor.setPassword(TestDatabaseSetup.SEED_PASSWORD_HASH);
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

    @Test
    void getProfessorById_StoredId_ReturnsMatchingProfessor() throws ManagerException {
        Professor resultProfessor = professorManager.getProfessorById(STORED_PROFESSOR_ID);

        assertEquals(STORED_PROFESSOR_ID, resultProfessor.getId());
    }

    @Test
    void updateProfessor_ValidPersonalData_DoesNotThrow() throws ManagerException {
        Professor professorToUpdate = professorManager.getProfessorById(STORED_PROFESSOR_ID);
        professorToUpdate.setName("Jose Eduardo Editado");

        assertDoesNotThrow(() -> professorManager.updateProfessor(professorToUpdate, STORED_PROFESSOR_ID));
    }

    @Test
    void updateProfessor_DuplicateEmail_ThrowsFriendlyMessageWithoutTechnicism() throws ManagerException {
        Professor professorToUpdate = professorManager.getProfessorById(STORED_PROFESSOR_ID);
        professorToUpdate.setEmail("mrodriguez@uv.mx");

        ManagerException thrownException = assertThrows(ManagerException.class,
                () -> professorManager.updateProfessor(professorToUpdate, STORED_PROFESSOR_ID));

        String message = thrownException.getMessage().toLowerCase();
        assertTrue(message.contains("correo"),
                "El mensaje debe indicar que el correo ya está en uso: " + thrownException.getMessage());
        assertFalse(leaksTechnicism(message),
                "El mensaje no debe filtrar tecnicismos de base de datos: " + thrownException.getMessage());
    }

    private boolean leaksTechnicism(String message) {
        return message.contains("duplicate") || message.contains("constraint")
                || message.contains("sql") || message.contains("exception")
                || message.contains("mysql") || message.contains(".java");
    }

    @Test
    void inactivateProfessor_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> professorManager.inactivateProfessor(STORED_PROFESSOR_ID));
    }

    @Test
    void activateProfessor_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> professorManager.activateProfessor(STORED_PROFESSOR_ID));
    }
}
