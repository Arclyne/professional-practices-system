package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IAdministratorDAO;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class AdministratorDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IAdministratorDAO administratorDAO;

    private Administrator testAdministrator;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testAdministrator = new Administrator();
        testAdministrator.setUserName("adminTest01");
        testAdministrator.setEmail("adminTest01@uv.mx");
        testAdministrator.setName("Admin");
        testAdministrator.setLastName("Test");
        testAdministrator.setPassword("adminPass123");
        testAdministrator.setRole("Administrator");
        testAdministrator.setStatus(UserStatus.ACTIVE);
        testAdministrator.setGender(Gender.MALE);
    }

    @Test
    void checkIfAdminExists_WithExistingAdmin_ReturnsTrue() throws DAOException {

        boolean result = administratorDAO.checkIfAdminExists();

        assertTrue(result);
    }

    @Test
    void insertAdministrator_ValidAdministrator_ReturnsGeneratedId() throws DAOException {

        int resultId = administratorDAO.insertAdministrator(testAdministrator);

        assertTrue(resultId > 0);
    }

    @Test
    void recoverAdministrator_ExistingId_ReturnsAdministrator() throws DAOException {
        int generatedId = administratorDAO.insertAdministrator(testAdministrator);

        Administrator recovered = administratorDAO.recoverAdministrator(generatedId);

        assertEquals(testAdministrator, recovered);
    }

    @Test
    void getAllAdministrators_WithExistingData_ReturnsList() throws DAOException {

        List<Administrator> resultList = administratorDAO.getAllAdministrators();

        assertFalse(resultList.isEmpty());
    }

    @Test
    void updateAdministrator_ValidModifiedData_ReturnsTrue() throws DAOException {
        int generatedId = administratorDAO.insertAdministrator(testAdministrator);
        testAdministrator.setName("Admin Modificado");
        testAdministrator.setStatus(UserStatus.INACTIVE);

        boolean result = administratorDAO.updateAdministrator(testAdministrator, generatedId);

        assertTrue(result);
    }
}