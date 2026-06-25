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
import mx.uv.fei.dataaccess.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class CoordinatorDAOTest {

    private static final int STORED_COORDINATOR_ID = 67;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ICoordinatorDAO coordinatorDAO;

    private Coordinator newCoordinator;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newCoordinator = new Coordinator();
        newCoordinator.setName("Patricia");
        newCoordinator.setLastName("Luna Mendez");
        newCoordinator.setUserName("pluna");
        newCoordinator.setEmail("pluna@uv.mx");
        newCoordinator.setPassword("CoordUv2026");
        newCoordinator.setRole("Coordinator");
        // Un segundo coordinador no puede estar activo (ya hay uno activo sembrado): se da de alta como
        // PENDING, igual que en el flujo real de registro.
        newCoordinator.setStatus(UserStatus.PENDING);
        newCoordinator.setGender(Gender.FEMALE);
    }

    private Coordinator buildStoredCoordinator() {
        Coordinator storedCoordinator = new Coordinator();
        storedCoordinator.setId(STORED_COORDINATOR_ID);
        storedCoordinator.setUserName("30022222");
        storedCoordinator.setPassword("CoordFei2026");
        storedCoordinator.setName("Marco Antonio");
        storedCoordinator.setLastName("Rodriguez Castillo");
        storedCoordinator.setEmail("mrodriguez@uv.mx");
        storedCoordinator.setRole("Coordinator");
        storedCoordinator.setStatus(UserStatus.ACTIVE);
        storedCoordinator.setGender(Gender.MALE);
        return storedCoordinator;
    }

    @Test
    void insertCoordinator_ValidCoordinator_ReturnsGeneratedId() throws DAOException {
        int resultId = coordinatorDAO.insertCoordinator(newCoordinator);

        assertTrue(resultId > 0);
    }

    @Test
    void recoverCoordinator_ExistingId_ReturnsCoordinator() throws DAOException {
        Coordinator expectedCoordinator = buildStoredCoordinator();

        Coordinator recoveredCoordinator = coordinatorDAO.recoverCoordinator(STORED_COORDINATOR_ID);

        assertEquals(expectedCoordinator, recoveredCoordinator);
    }

    @Test
    void getCurrentCoordinator_WithActiveCoordinator_ReturnsCoordinator() throws DAOException {
        Coordinator expectedCoordinator = buildStoredCoordinator();

        Coordinator currentCoordinator = coordinatorDAO.getCurrentCoordinator();

        assertEquals(expectedCoordinator, currentCoordinator);
    }

    @Test
    void getAllCoordinators_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Coordinator> expectedCoordinators = new ArrayList<>();
        expectedCoordinators.add(buildStoredCoordinator());

        List<Coordinator> resultCoordinators = coordinatorDAO.getAllCoordinators();

        assertEquals(expectedCoordinators, resultCoordinators);
    }

    @Test
    void updateCoordinator_ValidModifiedData_DoesNotThrow() throws DAOException {
        newCoordinator.setName("Patricia Isabel");
        newCoordinator.setStatus(UserStatus.INACTIVE);

        assertDoesNotThrow(() -> coordinatorDAO.updateCoordinator(newCoordinator, STORED_COORDINATOR_ID));
    }

    @Test
    void insertCoordinator_DuplicateUsername_ThrowsDAOException() {
        Coordinator duplicateUserNameCoordinator = buildStoredCoordinator();
        duplicateUserNameCoordinator.setEmail("correo.disponible@uv.mx");

        assertThrows(DAOException.class, () -> coordinatorDAO.insertCoordinator(duplicateUserNameCoordinator));
    }

    @Test
    void insertCoordinator_DuplicateEmail_ThrowsDAOException() {
        Coordinator duplicateEmailCoordinator = buildStoredCoordinator();
        duplicateEmailCoordinator.setUserName("usuarioNuevo");

        assertThrows(DAOException.class, () -> coordinatorDAO.insertCoordinator(duplicateEmailCoordinator));
    }

    @Test
    void recoverCoordinator_NonExistentId_ReturnsEmptyCoordinator() throws DAOException {
        Coordinator recoveredCoordinator = coordinatorDAO.recoverCoordinator(NON_EXISTENT_ID);

        assertEquals(new Coordinator(), recoveredCoordinator);
    }

    @Test
    void updateCoordinator_NonExistentId_ThrowsDAOException() {
        newCoordinator.setName("Patricia Isabel");

        assertThrows(DAOException.class, () -> coordinatorDAO.updateCoordinator(newCoordinator, NON_EXISTENT_ID));
    }
}
