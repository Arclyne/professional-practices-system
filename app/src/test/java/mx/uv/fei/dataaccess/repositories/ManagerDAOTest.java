package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IManagerDAO;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ManagerDAOTest {

    private static final int FIRST_ORGANIZATION_ID = 1;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IManagerDAO managerDAO;

    private Manager newManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newManager = new Manager();
        newManager.setName("Alejandro Vergara Soto");
        newManager.setPhone("2281234567");
        newManager.setEmail("alejandro.vergara@tecgolfo.mx");
        newManager.setStatus(UserStatus.ACTIVE);
        newManager.setOrganizationId(FIRST_ORGANIZATION_ID);
    }

    private Manager buildFirstStoredManager() {
        Manager storedManager = new Manager();
        storedManager.setId(1);
        storedManager.setName("Roberto Sanchez Luna");
        storedManager.setOrganizationId(1);
        storedManager.setStatus(UserStatus.ACTIVE);
        return storedManager;
    }

    @Test
    void insertManager_ValidManager_ReturnsTrue() throws DAOException {
        boolean isInserted = managerDAO.insertManager(newManager);

        assertTrue(isInserted);
    }

    @Test
    void getAllManagers_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Manager> expectedManagers = new ArrayList<>();
        expectedManagers.add(buildFirstStoredManager());

        Manager secondManager = new Manager();
        secondManager.setId(2);
        secondManager.setName("Maria Fernanda Ortiz Cabrera");
        secondManager.setOrganizationId(2);
        secondManager.setStatus(UserStatus.ACTIVE);
        expectedManagers.add(secondManager);

        Manager thirdManager = new Manager();
        thirdManager.setId(3);
        thirdManager.setName("Carlos Dominguez Ruiz");
        thirdManager.setOrganizationId(3);
        thirdManager.setStatus(UserStatus.ACTIVE);
        expectedManagers.add(thirdManager);

        List<Manager> resultManagers = managerDAO.getAllManagers();

        assertEquals(expectedManagers, resultManagers);
    }

    @Test
    void getManagersByOrganization_ExistingOrganization_ReturnsExpectedList() throws DAOException {
        List<Manager> expectedManagers = new ArrayList<>();
        expectedManagers.add(buildFirstStoredManager());

        List<Manager> resultManagers = managerDAO.getManagersByOrganization(FIRST_ORGANIZATION_ID);

        assertEquals(expectedManagers, resultManagers);
    }

    @Test
    void deactivateMultipleManagers_ValidIds_ReturnsTrue() throws DAOException {
        boolean isDeactivated = managerDAO.deactivateMultipleManagers(List.of(1, 2));

        assertTrue(isDeactivated);
    }

    @Test
    void insertManager_NonExistentOrganization_ThrowsDAOException() {
        newManager.setOrganizationId(NON_EXISTENT_ID);

        assertThrows(DAOException.class, () -> managerDAO.insertManager(newManager));
    }

    @Test
    void getManagersByOrganization_NonExistentOrganization_ReturnsEmptyList() throws DAOException {
        List<Manager> resultManagers = managerDAO.getManagersByOrganization(NON_EXISTENT_ID);

        assertEquals(new ArrayList<>(), resultManagers);
    }
}
