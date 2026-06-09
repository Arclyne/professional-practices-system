package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;
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

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ManagerManager managerManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void getAllManagers_ReturnsList() {
        assertDoesNotThrow(() -> managerManager.getAllManagers());
    }

    @Test
    void registerManager_ValidData_DoesNotThrow() {
        Manager manager = new Manager();
        manager.setName("Manager");
        manager.setPhone("2281234567");
        manager.setEmail("man@uv.mx");
        manager.setStatus(UserStatus.ACTIVE);
        manager.setOrganizationId(1);
        assertDoesNotThrow(() -> managerManager.registerManager(manager));
    }

    @Test
    void getManagersByOrganization_ValidId_ReturnsList() throws ManagerException {
        assertNotNull(managerManager.getManagersByOrganization(1));
    }

    @Test
    void inactivateMultipleManagers_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> managerManager.inactivateMultipleManagers(List.of(1)));
    }
}
