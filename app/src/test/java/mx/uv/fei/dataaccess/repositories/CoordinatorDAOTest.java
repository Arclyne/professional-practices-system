package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertTrue(resultId > 0);
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
        assertEquals(expected, recovered);
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
        assertEquals(expected, current);
    }

    @Test
    void getAllCoordinators_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Coordinator> expectedList = new ArrayList<>();
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
        expectedList.add(expected);

        List<Coordinator> resultList = coordinatorDAO.getAllCoordinators();
        assertEquals(expectedList, resultList);
    }

    @Test
    void updateCoordinator_ValidModifiedData_ReturnsTrue() throws DAOException {
        testCoordinator.setName("Angel Gabriel Modificado");
        testCoordinator.setStatus(UserStatus.INACTIVE);
        boolean isUpdated = coordinatorDAO.updateCoordinator(testCoordinator, 67);
        assertTrue(isUpdated);
    }

    @Test
    void insertCoordinator_DuplicateUsername_ThrowsDAOException() {
        Coordinator duplicate = new Coordinator();
        duplicate.setUserName("coord1");
        duplicate.setPassword("password");
        duplicate.setName("Clon");
        duplicate.setLastName("Test");
        duplicate.setEmail("nuevo_coord@uv.mx");
        duplicate.setRole("Coordinator");
        duplicate.setStatus(UserStatus.ACTIVE);
        duplicate.setGender(Gender.MALE);

        assertThrows(DAOException.class, () -> coordinatorDAO.insertCoordinator(duplicate));
    }

    @Test
    void insertCoordinator_DuplicateEmail_ThrowsDAOException() {
        Coordinator duplicate = new Coordinator();
        duplicate.setUserName("nuevoCoord02");
        duplicate.setPassword("password");
        duplicate.setName("Clon");
        duplicate.setLastName("Test");
        duplicate.setEmail("coord1@uv.mx");
        duplicate.setRole("Coordinator");
        duplicate.setStatus(UserStatus.ACTIVE);
        duplicate.setGender(Gender.MALE);

        assertThrows(DAOException.class, () -> coordinatorDAO.insertCoordinator(duplicate));
    }

    @Test
    void recoverCoordinator_NonExistentId_ReturnsEmptyCoordinator() throws DAOException {
        Coordinator recovered = coordinatorDAO.recoverCoordinator(9999);
        assertEquals(new Coordinator(), recovered);
    }

    @Test
    void updateCoordinator_NonExistentId_ReturnsFalse() throws DAOException {
        testCoordinator.setName("Fantasma");
        boolean result = coordinatorDAO.updateCoordinator(testCoordinator, 9999);
        assertFalse(result);
    }
}