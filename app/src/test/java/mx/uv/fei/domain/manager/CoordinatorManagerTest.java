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

    private static final int STORED_COORDINATOR_ID = 67;

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
        Coordinator newCoordinator = new Coordinator();
        newCoordinator.setName("Patricia");
        newCoordinator.setLastName("Luna Mendez");
        newCoordinator.setUserName("pluna");
        newCoordinator.setPassword("CoordUv2026");
        newCoordinator.setEmail("pluna@uv.mx");
        newCoordinator.setGender(Gender.FEMALE);

        assertDoesNotThrow(() -> coordinatorManager.registerNewCoordinator(newCoordinator));
    }

    @Test
    void inactivateCoordinator_ExistingId_DoesNotThrow() {
        assertDoesNotThrow(() -> coordinatorManager.inactivateCoordinator(STORED_COORDINATOR_ID));
    }

    @Test
    void retrieveCurrentCoordinator_ReturnsCoordinator() throws ManagerException {
        Coordinator expectedCoordinator = new Coordinator();
        expectedCoordinator.setId(STORED_COORDINATOR_ID);
        expectedCoordinator.setUserName("mrodriguez");
        expectedCoordinator.setPassword("CoordFei2026");
        expectedCoordinator.setName("Marco Antonio");
        expectedCoordinator.setLastName("Rodriguez Castillo");
        expectedCoordinator.setEmail("mrodriguez@uv.mx");
        expectedCoordinator.setRole("Coordinator");
        expectedCoordinator.setStatus(UserStatus.ACTIVE);
        expectedCoordinator.setGender(Gender.MALE);

        Coordinator currentCoordinator = coordinatorManager.retrieveCurrentCoordinator();

        assertEquals(expectedCoordinator, currentCoordinator);
    }
}
