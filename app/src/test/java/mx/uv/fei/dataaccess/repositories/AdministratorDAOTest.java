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

        assertTrue(result, "Debería retornar true porque el script ya insertó un administrador");
    }

    @Test
    void insertAdministrator_ValidAdministrator_ReturnsGeneratedId() throws DAOException {
        int resultId = administratorDAO.insertAdministrator(testAdministrator);

        assertTrue(resultId > 0, "El ID generado debería ser mayor a 0");
    }

    @Test
    void recoverAdministrator_ExistingId_ReturnsAdministrator() throws DAOException {
        Administrator expected = new Administrator();
        expected.setId(13);
        expected.setUserName("12345");
        expected.setPassword("12345");
        expected.setName("adm");
        expected.setLastName("adm");
        expected.setEmail("adm@adm.com");
        expected.setRole("Administrator");
        expected.setStatus(UserStatus.ACTIVE);
        expected.setGender(Gender.MALE);

        Administrator recovered = administratorDAO.recoverAdministrator(13);

        assertEquals(expected, recovered, "El administrador recuperado debe coincidir exactamente con el del script");
    }

    @Test
    void getAllAdministrators_WithExistingData_ReturnsList() throws DAOException {
        List<Administrator> resultList = administratorDAO.getAllAdministrators();

        assertFalse(resultList.isEmpty(), "La lista de administradores no debería estar vacía");
    }

    @Test
    void updateAdministrator_ValidModifiedData_ReturnsTrue() throws DAOException {
        Administrator adminToUpdate = new Administrator();
        adminToUpdate.setId(13);
        adminToUpdate.setUserName("12345");
        adminToUpdate.setPassword("12345");
        adminToUpdate.setName("Admin Modificado");
        adminToUpdate.setLastName("adm");
        adminToUpdate.setEmail("adm@adm.com");
        adminToUpdate.setRole("Administrator");
        adminToUpdate.setStatus(UserStatus.INACTIVE);
        adminToUpdate.setGender(Gender.MALE);

        boolean isUpdated = administratorDAO.updateAdministrator(adminToUpdate, 13);

        assertTrue(isUpdated, "La actualización debería retornar true para un registro existente modificado");
    }

    @Test
    void insertAdministrator_DuplicateUsername_ThrowsDAOException() {
        Administrator duplicateUsernameAdmin = new Administrator();
        duplicateUsernameAdmin.setUserName("12345");
        duplicateUsernameAdmin.setEmail("nuevo.correo@uv.mx");
        duplicateUsernameAdmin.setName("Clon");
        duplicateUsernameAdmin.setLastName("Test");
        duplicateUsernameAdmin.setPassword("password");
        duplicateUsernameAdmin.setRole("Administrator");
        duplicateUsernameAdmin.setStatus(UserStatus.ACTIVE);
        duplicateUsernameAdmin.setGender(Gender.MALE);

        assertThrows(DAOException.class, () -> {
            administratorDAO.insertAdministrator(duplicateUsernameAdmin);
        }, "Debería lanzar DAOException por violación de restricción UNIQUE en username");
    }

    @Test
    void insertAdministrator_DuplicateEmail_ThrowsDAOException() {
        Administrator duplicateEmailAdmin = new Administrator();
        duplicateEmailAdmin.setUserName("nuevoUsuario01");
        duplicateEmailAdmin.setEmail("adm@adm.com");
        duplicateEmailAdmin.setName("Clon");
        duplicateEmailAdmin.setLastName("Test");
        duplicateEmailAdmin.setPassword("password");
        duplicateEmailAdmin.setRole("Administrator");
        duplicateEmailAdmin.setStatus(UserStatus.ACTIVE);
        duplicateEmailAdmin.setGender(Gender.MALE);

        assertThrows(DAOException.class, () -> {
            administratorDAO.insertAdministrator(duplicateEmailAdmin);
        }, "Debería lanzar DAOException por violación de restricción UNIQUE en email");
    }

    @Test
    void recoverAdministrator_NonExistentId_ReturnsEmptyAdministrator() throws DAOException {
        int nonExistentId = 9999;
        Administrator expectedEmpty = new Administrator();

        Administrator recovered = administratorDAO.recoverAdministrator(nonExistentId);

        assertEquals(expectedEmpty, recovered, "Si el ID no existe, debe retornar un objeto Administrator inicializado vacío");
    }

    @Test
    void updateAdministrator_NonExistentId_ReturnsFalse() throws DAOException {
        int nonExistentId = 9999;
        testAdministrator.setName("Ghost Admin");

        boolean result = administratorDAO.updateAdministrator(testAdministrator, nonExistentId);

        assertFalse(result, "La actualización debería retornar false si el ID especificado no existe");
    }
}
