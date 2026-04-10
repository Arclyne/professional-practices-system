package mx.uv.fei.dataacces.repositories;


import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;


import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.dataacces.exceptions.DAOException;


public class CoordinatorDAOTest {
    private CoordinatorDAO coordinatorDAO;
    private Coordinator testCoordinator;

    @BeforeEach
    void setUp() {
        coordinatorDAO = new CoordinatorDAO();

        testCoordinator = new Coordinator();
        testCoordinator.setName("Angel");
        testCoordinator.setLastName("Aguilar");
        testCoordinator.setPassword("securepass");
        testCoordinator.setStatus("activo");
        testCoordinator.setGender("Masculino");
    }

    @Test
    void testInsertCoordinatorSuccess() {
        try {
            boolean result = coordinatorDAO.insertCoordinator(testCoordinator);
            
            assertTrue(result, "El coordinador debió registrarse exitosamente en ambas tablas");
            
        } catch (DAOException e) {
            fail("Falló la inserción del coordinador: " + e.getMessage());
        }
    }
}