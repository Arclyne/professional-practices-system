package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ManagerManagerTest {

    private static final int FIRST_ORGANIZATION_ID = 1;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ManagerManager managerManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
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
    void getAllManagers_ReturnsExpectedList() throws ManagerException {
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

        List<Manager> resultManagers = managerManager.getAllManagers();

        assertEquals(expectedManagers, resultManagers);
    }

    @Test
    void registerManager_ValidData_DoesNotThrow() {
        Manager newManager = new Manager();
        newManager.setName("Alejandro Vergara Soto");
        newManager.setPhone("2281234567");
        newManager.setEmail("alejandro.vergara@tecgolfo.mx");
        newManager.setStatus(UserStatus.ACTIVE);
        newManager.setOrganizationId(FIRST_ORGANIZATION_ID);

        assertDoesNotThrow(() -> managerManager.registerManager(newManager));
    }

    @Test
    void getManagersByOrganization_ValidId_ReturnsExpectedList() throws ManagerException {
        List<Manager> expectedManagers = new ArrayList<>();
        expectedManagers.add(buildFirstStoredManager());

        List<Manager> resultManagers = managerManager.getManagersByOrganization(FIRST_ORGANIZATION_ID);

        assertEquals(expectedManagers, resultManagers);
    }

    @Test
    void inactivateMultipleManagers_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> managerManager.inactivateMultipleManagers(List.of(1)));
    }

    @Test
    void inactivateManager_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> managerManager.inactivateManager(1));
    }

    @Test
    void activateManager_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> managerManager.activateManager(1));
    }

    @Test
    void getManagerById_StoredId_ReturnsMatchingManager() throws ManagerException {
        Manager resultManager = managerManager.getManagerById(1);

        assertEquals(1, resultManager.getId());
    }

    @Test
    void updateManager_ValidData_DoesNotThrow() {
        Manager managerToUpdate = new Manager();
        managerToUpdate.setName("Roberto Sanchez Editado");
        managerToUpdate.setPhone("2289998877");
        managerToUpdate.setEmail("rsanchez@tecgolfo.mx");
        managerToUpdate.setOrganizationId(FIRST_ORGANIZATION_ID);

        assertDoesNotThrow(() -> managerManager.updateManager(managerToUpdate, 1));
    }
}
