package mx.uv.fei.dataaccess.repositories;

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
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IManagerDAO;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")

public class ManagerDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IManagerDAO managerDAO;

    private Manager testManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testManager = new Manager();
        testManager.setName("Manager Test");
        testManager.setPhone("2281234567");
        testManager.setEmail("managertest@empresa.mx");
        testManager.setStatus(UserStatus.ACTIVE);
        testManager.setOrganizationId(1);
    }

    @Test
    void insertManager_ValidManager_ReturnsTrue() throws DAOException {
        boolean isInserted = managerDAO.insertManager(testManager);
        assertTrue(isInserted);
    }

    @Test
    void getAllManagers_WithExistingData_ReturnsList() throws DAOException {
        List<Manager> resultList = managerDAO.getAllManagers();
        assertFalse(resultList.isEmpty());
    }

    @Test
    void getManagersByOrganization_ExistingOrganization_ReturnsList() throws DAOException {
        List<Manager> resultList = managerDAO.getManagersByOrganization(1);
        assertFalse(resultList.isEmpty());
    }

    @Test
    void deactivateMultipleManagers_ValidIds_ReturnsTrue() throws DAOException {
        boolean isDeactivated = managerDAO.deactivateMultipleManagers(List.of(1, 2));
        assertTrue(isDeactivated);
    }

    @Test
    void insertManager_NonExistentOrganization_ThrowsDAOException() {
        testManager.setOrganizationId(9999);
        assertThrows(DAOException.class, () -> {
            managerDAO.insertManager(testManager);
        });
    }

    @Test
    void getManagersByOrganization_NonExistentOrganization_ReturnsEmptyList() throws DAOException {
        List<Manager> resultList = managerDAO.getManagersByOrganization(9999);
        assertTrue(resultList.isEmpty());
    }
}
