package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

public class CoordinatorDAOIT {

    private IDatabaseConnection dbConnection;
    private DatabasePropeties propeties;
    private ICoordinatorDAO coordinatorDAOTest;
    private UserDAO userTestDao;
    private Coordinator testCoordinator;

    @BeforeEach
    void setUp() throws SQLException {
        userTestDao = new UserDAO(dbConnection);
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        TestDatabaseSetup.initialize(dbConnection);
        coordinatorDAOTest = new CoordinatorDAO(dbConnection, userTestDao);
        testCoordinator = new Coordinator();

        testCoordinator.setName("Angel");
        testCoordinator.setLastName("Aguilar");
        testCoordinator.setPassword("securepass");
        testCoordinator.setStatus("Activo");
        testCoordinator.setGender("Masculino");
    }

    @Test
    void testInsertCoordinatorSuccess() throws DAOException {
        int resultId = coordinatorDAOTest.insertCoordinator(testCoordinator);

        assertTrue(resultId > 0, "El coordinador debió registrarse exitosamente y devolver un ID mayor a 0");
    }

    @Test
    void testRecoverCoordinatorSuccess() throws DAOException {
        int generatedId = coordinatorDAOTest.insertCoordinator(testCoordinator);

        Coordinator recovered = coordinatorDAOTest.recoverCoordinator(generatedId);

        assertEquals(testCoordinator, recovered, "El coordinador recuperado no coincide con el insertado.");
    }

    @Test
    void testGetAllCoordinatorsSuccess() throws DAOException {
        coordinatorDAOTest.insertCoordinator(testCoordinator);

        List<Coordinator> list = coordinatorDAOTest.getAllCoordinators();

        assertFalse(list.isEmpty(), "La lista debe contener al menos al coordinador que acabamos de insertar");
    }

    @Test
    void testUpdateCoordinatorSuccess() throws DAOException {
        int generatedId = coordinatorDAOTest.insertCoordinator(testCoordinator);

        testCoordinator.setName("Angel Gabriel");
        testCoordinator.setStatus("No Activo");

        coordinatorDAOTest.updateCoordinator(testCoordinator, generatedId);

        Coordinator recovered = coordinatorDAOTest.recoverCoordinator(generatedId);
        assertEquals(testCoordinator, recovered, "Los datos del coordinador recuperado no reflejan la actualización.");
    }
}