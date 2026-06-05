package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;

@StartEtiquetteTest
@Profile("test")
public class CoordinatorDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ICoordinatorDAO coordinatorDAO;

    private Coordinator testCoordinator;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection);
        assertNotNull(coordinatorDAO);
        TestDatabaseSetup.initialize(dbConnection);
        testCoordinator = new Coordinator();
        testCoordinator.setName("Angel");
        testCoordinator.setLastName("Aguilar");
        testCoordinator.setPassword("securepass");
        testCoordinator.setStatus(UserStatus.valueOf("Activo"));
        testCoordinator.setGender(Gender.valueOf("Masculino"));
    }

    @Test
    void insertCoordinator_ValidCoordinator_ReturnsGeneratedId() throws DAOException {

        int resultId = coordinatorDAO.insertCoordinator(testCoordinator);

        assertTrue(resultId > 0);
    }

    @Test
    void recoverCoordinator_ExistingId_ReturnsCoordinator() throws DAOException {
        int generatedId = coordinatorDAO.insertCoordinator(testCoordinator);

        Coordinator recovered = coordinatorDAO.recoverCoordinator(generatedId);

        assertEquals(testCoordinator, recovered);
    }

    @Test
    void getAllCoordinators_WithExistingData_ReturnsList() throws DAOException {
        coordinatorDAO.insertCoordinator(testCoordinator);

        List<Coordinator> list = coordinatorDAO.getAllCoordinators();

        assertFalse(list.isEmpty());
    }

    @Test
    void updateCoordinator_ValidModifiedData_ReturnsUpdatedCoordinator() throws DAOException {
        int generatedId = coordinatorDAO.insertCoordinator(testCoordinator);
        testCoordinator.setName("Angel Gabriel");
        testCoordinator.setStatus(UserStatus.valueOf("No Activo"));

        coordinatorDAO.updateCoordinator(testCoordinator, generatedId);
        Coordinator recovered = coordinatorDAO.recoverCoordinator(generatedId);

        assertEquals(testCoordinator, recovered);
    }
}