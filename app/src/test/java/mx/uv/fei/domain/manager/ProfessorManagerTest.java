package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.Gender;
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
        assertNotNull(assertDoesNotThrow(() -> professorManager.registerNewProfessor(p)));
    }

    @Test
    void getAllProfessors_ReturnsList() throws ManagerException {
        assertNotNull(professorManager.getAllProfessors());
    }

    @Test
    void inactivateMultipleProfessors_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> professorManager.inactivateMultipleProfessors(java.util.List.of(68)));
    }
}