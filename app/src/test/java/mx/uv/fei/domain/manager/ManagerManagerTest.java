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

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ManagerManager managerManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void getAllManagers_ReturnsExpectedList() throws ManagerException {
        List<Manager> expectedList = new ArrayList<>();

        Manager m1 = new Manager();
        m1.setId(1);
        m1.setName("Manager toRecover");
        m1.setOrganizationId(1);
        m1.setStatus(UserStatus.ACTIVE);
        expectedList.add(m1);

        Manager m2 = new Manager();
        m2.setId(2);
        m2.setName("Manager Dummy 1");
        m2.setOrganizationId(2);
        m2.setStatus(UserStatus.ACTIVE);
        expectedList.add(m2);

        Manager m3 = new Manager();
        m3.setId(3);
        m3.setName("Manager Dummy 2");
        m3.setOrganizationId(3);
        m3.setStatus(UserStatus.ACTIVE);
        expectedList.add(m3);

        List<Manager> resultList = managerManager.getAllManagers();
        assertEquals(expectedList, resultList);
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
    void getManagersByOrganization_ValidId_ReturnsExpectedList() throws ManagerException {
        List<Manager> expectedList = new ArrayList<>();

        Manager m1 = new Manager();
        m1.setId(1);
        m1.setName("Manager toRecover");
        m1.setOrganizationId(1);
        m1.setStatus(UserStatus.ACTIVE);
        expectedList.add(m1);

        List<Manager> resultList = managerManager.getManagersByOrganization(1);
        assertEquals(expectedList, resultList);
    }

    @Test
    void inactivateMultipleManagers_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> managerManager.inactivateMultipleManagers(List.of(1)));
    }
}