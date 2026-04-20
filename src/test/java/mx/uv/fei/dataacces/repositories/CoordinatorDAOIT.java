package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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