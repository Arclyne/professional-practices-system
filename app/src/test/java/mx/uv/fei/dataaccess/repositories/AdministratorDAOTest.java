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

    private static final int STORED_ADMINISTRATOR_ID = 13;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IAdministratorDAO administratorDAO;

    private Administrator newAdministrator;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newAdministrator = new Administrator();
        newAdministrator.setUserName("gtorres");
        newAdministrator.setEmail("gtorres@uv.mx");
        newAdministrator.setName("Guadalupe");
        newAdministrator.setLastName("Torres Lagunes");
        newAdministrator.setPassword("AdminUv2026");
        newAdministrator.setRole("Administrator");
        newAdministrator.setStatus(UserStatus.ACTIVE);
        newAdministrator.setGender(Gender.FEMALE);
    }

    private Administrator buildStoredAdministrator() {
        Administrator storedAdministrator = new Administrator();
        storedAdministrator.setId(STORED_ADMINISTRATOR_ID);
        storedAdministrator.setUserName("rmarquez");
        storedAdministrator.setPassword("AdminFei2026");
        storedAdministrator.setName("Ricardo");
        storedAdministrator.setLastName("Marquez Sosa");
        storedAdministrator.setEmail("rmarquez@uv.mx");
        storedAdministrator.setRole("Administrator");
        storedAdministrator.setStatus(UserStatus.ACTIVE);
        storedAdministrator.setGender(Gender.MALE);
        return storedAdministrator;
    }

    @Test
    void checkIfAdminExists_WithExistingAdmin_ReturnsTrue() throws DAOException {
        boolean result = administratorDAO.checkIfAdminExists();

        assertTrue(result, "Deberia retornar true porque el script ya inserto un administrador");
    }

    @Test
    void insertAdministrator_ValidAdministrator_ReturnsGeneratedId() throws DAOException {
        int resultId = administratorDAO.insertAdministrator(newAdministrator);

        assertTrue(resultId > 0, "El ID generado deberia ser mayor a 0");
    }

    @Test
    void recoverAdministrator_ExistingId_ReturnsAdministrator() throws DAOException {
        Administrator expectedAdministrator = buildStoredAdministrator();

        Administrator recoveredAdministrator = administratorDAO.recoverAdministrator(STORED_ADMINISTRATOR_ID);

        assertEquals(expectedAdministrator, recoveredAdministrator);
    }

    @Test
    void getAllAdministrators_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Administrator> expectedAdministrators = new ArrayList<>();
        expectedAdministrators.add(buildStoredAdministrator());

        List<Administrator> resultAdministrators = administratorDAO.getAllAdministrators();

        assertEquals(expectedAdministrators, resultAdministrators);
    }

    @Test
    void updateAdministrator_ValidModifiedData_ReturnsTrue() throws DAOException {
        Administrator administratorToUpdate = buildStoredAdministrator();
        administratorToUpdate.setName("Ricardo Alberto");
        administratorToUpdate.setStatus(UserStatus.INACTIVE);

        boolean isUpdated = administratorDAO.updateAdministrator(administratorToUpdate, STORED_ADMINISTRATOR_ID);

        assertTrue(isUpdated);
    }

    @Test
    void insertAdministrator_DuplicateUsername_ThrowsDAOException() {
        Administrator duplicateUserNameAdministrator = buildStoredAdministrator();
        duplicateUserNameAdministrator.setEmail("correo.disponible@uv.mx");

        assertThrows(DAOException.class, () -> administratorDAO.insertAdministrator(duplicateUserNameAdministrator));
    }

    @Test
    void insertAdministrator_DuplicateEmail_ThrowsDAOException() {
        Administrator duplicateEmailAdministrator = buildStoredAdministrator();
        duplicateEmailAdministrator.setUserName("usuarioNuevo");

        assertThrows(DAOException.class, () -> administratorDAO.insertAdministrator(duplicateEmailAdministrator));
    }

    @Test
    void recoverAdministrator_NonExistentId_ReturnsEmptyAdministrator() throws DAOException {
        Administrator recoveredAdministrator = administratorDAO.recoverAdministrator(NON_EXISTENT_ID);

        assertEquals(new Administrator(), recoveredAdministrator);
    }

    @Test
    void updateAdministrator_NonExistentId_ReturnsFalse() throws DAOException {
        newAdministrator.setName("Guadalupe Maria");

        boolean result = administratorDAO.updateAdministrator(newAdministrator, NON_EXISTENT_ID);

        assertFalse(result);
    }
}
