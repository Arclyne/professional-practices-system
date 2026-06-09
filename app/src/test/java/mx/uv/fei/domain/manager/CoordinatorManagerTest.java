package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
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
    void retrieveCurrentCoordinator_ReturnsCoordinator() {
        assertDoesNotThrow(() -> coordinatorManager.retrieveCurrentCoordinator());
    }

}
