package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class CoordinatorManagerTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private CoordinatorManager coordinatorManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewCoordinator_ValidData_ReturnsPassword() {
        Coordinator coordinator = new Coordinator();
        coordinator.setName("Coord");
        coordinator.setLastName("Test");
        coordinator.setUserName("12345678");
        coordinator.setPassword("Pass12345");
        coordinator.setEmail("coordtest@uv.mx");
        coordinator.setGender(Gender.MALE);

        assertDoesNotThrow(() -> coordinatorManager.registerNewCoordinator(coordinator));
    }

    @Test
    void inactivateCoordinator_ExistingId_DoesNotThrow() {
        assertDoesNotThrow(() -> coordinatorManager.inactivateCoordinator(67));
    }

    @Test
    void retrieveCurrentCoordinator_ReturnsCoordinator() throws ManagerException {
        Coordinator expected = new Coordinator();
        expected.setId(67);
        expected.setUserName("coord1");
        expected.setPassword("12345");
        expected.setName("Coord");
        expected.setLastName("Test");
        expected.setEmail("coord1@uv.mx");
        expected.setRole("Coordinator");
        expected.setStatus(UserStatus.ACTIVE);
        expected.setGender(Gender.MALE);

        Coordinator current = coordinatorManager.retrieveCurrentCoordinator();
        assertEquals(expected, current);
    }
}