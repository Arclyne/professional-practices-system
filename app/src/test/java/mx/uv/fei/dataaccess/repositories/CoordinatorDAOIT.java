package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.util.List;

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
public class CoordinatorDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ICoordinatorDAO coordinatorDAOTest;

    private Coordinator testCoordinator;

    @BeforeEach
    void setUp() throws SQLException {

        assertNotNull(dbConnection, "dbConnection no fue inyectado.");
        assertNotNull(coordinatorDAOTest, "coordinatorDAOTest no fue inyectado.");

        TestDatabaseSetup.initialize(dbConnection);

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
        assertTrue(resultId > 0, "El coordinador debió registrarse exitosamente y devolver un ID mayor a 0.");
    }

    @Test
    void testRecoverCoordinatorSuccess() throws DAOException {
        int generatedId = coordinatorDAOTest.insertCoordinator(testCoordinator);
        Coordinator recovered = coordinatorDAOTest.recoverCoordinator(generatedId);

        assertNotNull(recovered, "No se pudo recuperar el coordinador recién insertado.");
        assertEquals(testCoordinator, recovered, "El coordinador recuperado no coincide con el insertado.");
    }

    @Test
    void testGetAllCoordinatorsSuccess() throws DAOException {
        coordinatorDAOTest.insertCoordinator(testCoordinator);
        List<Coordinator> list = coordinatorDAOTest.getAllCoordinators();

        assertNotNull(list, "La lista de coordinadores no debería ser nula.");
        assertFalse(list.isEmpty(), "La lista debe contener al menos al coordinador que acabamos de insertar.");
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