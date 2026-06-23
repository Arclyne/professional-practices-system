package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void registerNewCoordinator_ActiveCoordinatorExists_ThrowsManagerException() {
        Coordinator newCoordinator = buildValidCoordinator();

        assertThrows(ManagerException.class, () -> coordinatorManager.registerNewCoordinator(newCoordinator));
    }

    @Test
    void registerNewCoordinator_NoActiveCoordinator_ReturnsPassword() throws ManagerException {
        coordinatorManager.inactivateCoordinator(STORED_COORDINATOR_ID);
        Coordinator newCoordinator = buildValidCoordinator();

        String temporaryPassword = coordinatorManager.registerNewCoordinator(newCoordinator);

        assertNotNull(temporaryPassword);
    }

    @Test
    void activateCoordinator_ActiveCoordinatorExists_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> coordinatorManager.activateCoordinator(STORED_COORDINATOR_ID));
    }

    @Test
    void activateCoordinator_NoActiveCoordinator_DoesNotThrow() throws ManagerException {
        coordinatorManager.inactivateCoordinator(STORED_COORDINATOR_ID);

        assertDoesNotThrow(() -> coordinatorManager.activateCoordinator(STORED_COORDINATOR_ID));
    }

    @Test
    void inactivateCoordinator_ExistingId_DoesNotThrow() {
        assertDoesNotThrow(() -> coordinatorManager.inactivateCoordinator(STORED_COORDINATOR_ID));
    }

    @Test
    void retrieveCurrentCoordinator_ReturnsCoordinator() throws ManagerException {
        Coordinator expectedCoordinator = new Coordinator();
        expectedCoordinator.setId(STORED_COORDINATOR_ID);
        expectedCoordinator.setUserName("30022222");
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

    @Test
    void getCoordinatorById_StoredId_ReturnsMatchingCoordinator() throws ManagerException {
        Coordinator resultCoordinator = coordinatorManager.getCoordinatorById(STORED_COORDINATOR_ID);

        assertEquals(STORED_COORDINATOR_ID, resultCoordinator.getId());
    }

    @Test
    void updateCoordinator_ValidPersonalData_DoesNotThrow() throws ManagerException {
        Coordinator coordinatorToUpdate = coordinatorManager.getCoordinatorById(STORED_COORDINATOR_ID);
        coordinatorToUpdate.setName("Marco Antonio Editado");

        assertDoesNotThrow(() -> coordinatorManager.updateCoordinator(coordinatorToUpdate, STORED_COORDINATOR_ID));
    }

    private Coordinator buildValidCoordinator() {
        Coordinator coordinator = new Coordinator();
        coordinator.setName("Patricia");
        coordinator.setLastName("Luna Mendez");
        coordinator.setUserName("30025566");
        coordinator.setPassword("CoordUv2026");
        coordinator.setEmail("pluna@uv.mx");
        coordinator.setGender(Gender.FEMALE);

        return coordinator;
    }
}
