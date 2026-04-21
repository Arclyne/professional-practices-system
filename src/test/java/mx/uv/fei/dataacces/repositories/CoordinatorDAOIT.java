package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.domain.dto.Coordinator;
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
    void setUp() {
        userTestDao = new UserDAO(dbConnection);
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        coordinatorDAOTest = new CoordinatorDAO(dbConnection, userTestDao);
        testCoordinator = new Coordinator();

        testCoordinator.setName("Angel");
        testCoordinator.setLastName("Aguilar");
        testCoordinator.setPassword("securepass");
        testCoordinator.setStatus("Activo");
        testCoordinator.setGender("Masculino");
    }

    @Test
    void testInsertCoordinatorSuccess() {
        try {
            int resultId = coordinatorDAOTest.insertCoordinator(testCoordinator);
            assertTrue(resultId > 0,
                    "El coordinador debió registrarse exitosamente en ambas tablas y devolver un ID mayor a 0");
        } catch (DAOException e) {
            fail("Falló la inserción del coordinador: " + e.getMessage());
        }
    }

    @Test
    void testRecoverCoordinatorSuccess() {
        try {
            int generatedId = coordinatorDAOTest.insertCoordinator(testCoordinator);

            Coordinator recovered = coordinatorDAOTest.recoverCoordinator(generatedId);

            assertNotNull(recovered, "El objeto recuperado no debería ser nulo");
            assertEquals("Angel", recovered.getName(), "El nombre debería coincidir");
            assertEquals("Aguilar", recovered.getLastName(), "Los apellidos deberían coincidir");
            assertNotNull(recovered.getRegistrationDate(),
                    "La fecha de registro debió generarse automáticamente en la BD");

        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testGetAllCoordinatorsSuccess() {
        try {
            coordinatorDAOTest.insertCoordinator(testCoordinator);

            List<Coordinator> list = coordinatorDAOTest.getAllCoordinators();

            assertTrue(list.size() > 0, "La lista debe contener al menos al coordinador que acabamos de insertar");
        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testUpdateCoordinatorSuccess() {
        try {
            int generatedId = coordinatorDAOTest.insertCoordinator(testCoordinator);

            testCoordinator.setName("Angel Gabriel");
            testCoordinator.setStatus("No Activo");

            boolean isUpdated = coordinatorDAOTest.updateCoordinator(testCoordinator, generatedId);
            assertTrue(isUpdated, "La actualización en la tabla USUARIO debió devolver true");

            Coordinator recovered = coordinatorDAOTest.recoverCoordinator(generatedId);
            assertEquals("Angel Gabriel", recovered.getName(), "El nombre debió actualizarse");
            assertEquals("No Activo", recovered.getStatus(), "El estado debió actualizarse a No Activo");

        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }
}