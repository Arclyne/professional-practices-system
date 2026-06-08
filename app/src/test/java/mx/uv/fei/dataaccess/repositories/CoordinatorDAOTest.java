package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
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

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ICoordinatorDAO coordinatorDAO;

    private Coordinator testCoordinator;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testCoordinator = new Coordinator();
        testCoordinator.setName("Angel");
        testCoordinator.setLastName("Aguilar");
        testCoordinator.setUserName("aanguilar");
        testCoordinator.setEmail("aanguilar@uv.mx");
        testCoordinator.setPassword("securepass");
        testCoordinator.setRole("Coordinator");
        testCoordinator.setStatus(UserStatus.ACTIVE);
        testCoordinator.setGender(Gender.MALE);
    }

    @Test
    void insertCoordinator_ValidCoordinator_ReturnsGeneratedId() throws DAOException {
        int resultId = coordinatorDAO.insertCoordinator(testCoordinator);

        assertTrue(resultId > 0, "El ID generado debería ser mayor a 0");
    }

    @Test
    void recoverCoordinator_ExistingId_ReturnsCoordinator() throws DAOException {
        Coordinator expected = new Coordinator();
        expected.setId(67);
        expected.setUserName("coord1");
        expected.setPassword("12345");
        expected.setName("Coord");
        expected.setLastName("Test");
        expected.setEmail("coord1@uv.mx");
        expected.setRole("Coordinator");
        expected.setStatus(UserStatus.ACTIVE);
        expected.setGender(Gender.MALE);

        Coordinator recovered = coordinatorDAO.recoverCoordinator(67);

        assertEquals(expected, recovered, "El coordinador recuperado debe coincidir exactamente con el del script");
    }

    @Test
    void getCurrentCoordinator_WithActiveCoordinator_ReturnsCoordinator() throws DAOException {
        Coordinator expected = new Coordinator();
        expected.setId(67);
        expected.setUserName("coord1");
        expected.setPassword("12345");
        expected.setName("Coord");
        expected.setLastName("Test");
        expected.setEmail("coord1@uv.mx");
        expected.setRole("Coordinator");
        expected.setStatus(UserStatus.ACTIVE);
        expected.setGender(Gender.MALE);

        Coordinator current = coordinatorDAO.getCurrentCoordinator();

        assertEquals(expected, current, "Debería recuperar el coordinador activo predefinido en el script");
    }

    @Test
    void getAllCoordinators_WithExistingData_ReturnsList() throws DAOException {
        List<Coordinator> resultList = coordinatorDAO.getAllCoordinators();

        assertFalse(resultList.isEmpty(), "La lista de coordinadores no debería estar vacía");
    }

    @Test
    void updateCoordinator_ValidModifiedData_ReturnsTrue() throws DAOException {
        testCoordinator.setName("Angel Gabriel Modificado");
        testCoordinator.setStatus(UserStatus.INACTIVE);

        boolean isUpdated = coordinatorDAO.updateCoordinator(testCoordinator, 67);

        assertTrue(isUpdated, "La actualización debe retornar true para un registro existente modificado");
    }

    @Test
    void insertCoordinator_DuplicateUsername_ThrowsDAOException() {
        Coordinator duplicateUsernameCoordinator = new Coordinator();
        duplicateUsernameCoordinator.setUserName("coord1");
        duplicateUsernameCoordinator.setEmail("nuevo_coord@uv.mx");
        duplicateUsernameCoordinator.setName("Clon");
        duplicateUsernameCoordinator.setLastName("Test");
        duplicateUsernameCoordinator.setPassword("password");
        duplicateUsernameCoordinator.setRole("Coordinator");
        duplicateUsernameCoordinator.setStatus(UserStatus.ACTIVE);
        duplicateUsernameCoordinator.setGender(Gender.MALE);

        assertThrows(DAOException.class, () -> {
            coordinatorDAO.insertCoordinator(duplicateUsernameCoordinator);
        }, "Debería lanzar DAOException por violación UNIQUE en username");
    }

    @Test
    void insertCoordinator_DuplicateEmail_ThrowsDAOException() {
        Coordinator duplicateEmailCoordinator = new Coordinator();
        duplicateEmailCoordinator.setUserName("nuevoCoord02");
        duplicateEmailCoordinator.setEmail("coord1@uv.mx");
        duplicateEmailCoordinator.setName("Clon");
        duplicateEmailCoordinator.setLastName("Test");
        duplicateEmailCoordinator.setPassword("password");
        duplicateEmailCoordinator.setRole("Coordinator");
        duplicateEmailCoordinator.setStatus(UserStatus.ACTIVE);
        duplicateEmailCoordinator.setGender(Gender.MALE);

        assertThrows(DAOException.class, () -> {
            coordinatorDAO.insertCoordinator(duplicateEmailCoordinator);
        }, "Debería lanzar DAOException por violación UNIQUE en email");
    }

    @Test
    void recoverCoordinator_NonExistentId_ReturnsEmptyCoordinator() throws DAOException {
        int nonExistentId = 9999;
        Coordinator expectedEmpty = new Coordinator();

        Coordinator recovered = coordinatorDAO.recoverCoordinator(nonExistentId);

        assertEquals(expectedEmpty, recovered, "Si el ID no existe, debe retornar un objeto Coordinator inicializado vacío");
    }

    @Test
    void updateCoordinator_NonExistentId_ReturnsFalse() throws DAOException {
        int nonExistentId = 9999;
        testCoordinator.setName("Fantasma");

        boolean result = coordinatorDAO.updateCoordinator(testCoordinator, nonExistentId);

        assertFalse(result, "La actualización debe retornar false al no encontrar el ID especificado");
    }
}