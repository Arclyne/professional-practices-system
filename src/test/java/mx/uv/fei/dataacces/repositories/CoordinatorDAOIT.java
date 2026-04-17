package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import mx.uv.fei.TestApp;
import mx.uv.fei.TestConfig;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.ICoordinatorDAO;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class CoordinatorDAOIT {

    @Autowired
    private ICoordinatorDAO coordinatorDAOTest;

    private Coordinator testCoordinator;

    @BeforeEach
    void setUp() {
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
            assertTrue(resultId > 0, "El coordinador debió registrarse exitosamente en ambas tablas y devolver un ID mayor a 0");
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
            assertNotNull(recovered.getRegistrationDate(), "La fecha de registro debió generarse automáticamente en la BD");
            
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