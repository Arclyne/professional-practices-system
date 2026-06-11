package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class UserDAOTest {

    private static final int STORED_ADMINISTRATOR_ID = 13;
    private static final int STORED_COORDINATOR_ID = 67;
    private static final String STORED_ADMIN_USERNAME = "rmarquez";
    private static final String STORED_ADMIN_EMAIL = "rmarquez@uv.mx";
    private static final String STORED_ADMIN_PASSWORD = "AdminFei2026";

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IUserDAO userDAO;

    private User newUser;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newUser = new User();
        newUser.setUserName("zS25080910");
        newUser.setEmail("zS25080910@estudiantes.uv.mx");
        newUser.setName("Luis Fernando");
        newUser.setLastName("Martinez Rivera");
        newUser.setPassword("PracticanteUv2026");
        newUser.setRole("Practitioner");
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setGender(Gender.MALE);
    }

    private User buildStoredAdministratorUser() {
        User storedUser = new User();
        storedUser.setId(STORED_ADMINISTRATOR_ID);
        storedUser.setUserName(STORED_ADMIN_USERNAME);
        storedUser.setPassword(STORED_ADMIN_PASSWORD);
        storedUser.setName("Ricardo");
        storedUser.setLastName("Marquez Sosa");
        storedUser.setEmail(STORED_ADMIN_EMAIL);
        storedUser.setRole("Administrator");
        storedUser.setStatus(UserStatus.ACTIVE);
        storedUser.setGender(Gender.MALE);
        return storedUser;
    }

    @Test
    void insertUser_ValidUser_ReturnsGeneratedId() throws SQLException, DAOException {
        try (Connection connection = dbConnection.getConnection()) {
            int generatedId = userDAO.insertUser(newUser, connection);

            assertTrue(generatedId > 0);
        }
    }

    @Test
    void deactivateUser_ExistingId_ReturnsTrue() throws DAOException {
        boolean result = userDAO.deactivateUser(STORED_ADMINISTRATOR_ID);

        assertTrue(result);
    }

    @Test
    void updateUser_ValidModifiedData_ReturnsTrue() throws SQLException, DAOException {
        try (Connection connection = dbConnection.getConnection()) {
            newUser.setId(STORED_ADMINISTRATOR_ID);
            newUser.setStatus(UserStatus.INACTIVE);
            newUser.setPassword("NuevaClaveUv2026");

            boolean result = userDAO.updateUser(newUser, connection);

            assertTrue(result);
        }
    }

    @Test
    void verifyCredentialsByUserName_ValidCredentials_ReturnsTrue() throws DAOException {
        boolean result = userDAO.verifyCredentialsByUserName(STORED_ADMIN_USERNAME, STORED_ADMIN_PASSWORD);

        assertTrue(result);
    }

    @Test
    void verifyCredentialsByEmail_ValidCredentials_ReturnsTrue() throws DAOException {
        boolean result = userDAO.verifyCredentialsByEmail(STORED_ADMIN_EMAIL, STORED_ADMIN_PASSWORD);

        assertTrue(result);
    }

    @Test
    void getUserRole_ExistingUser_ReturnsRole() throws DAOException {
        String role = userDAO.getUserRole(STORED_ADMIN_USERNAME);

        assertEquals("Administrator", role);
    }

    @Test
    void getUserByUserName_ExistingUser_ReturnsUser() throws DAOException {
        User expectedUser = buildStoredAdministratorUser();

        User recoveredUser = userDAO.getUserByUserName(STORED_ADMIN_USERNAME);

        assertEquals(expectedUser, recoveredUser);
    }

    @Test
    void getUserByEmail_ExistingUser_ReturnsUser() throws DAOException {
        User expectedUser = buildStoredAdministratorUser();

        User recoveredUser = userDAO.getUserByEmail(STORED_ADMIN_EMAIL);

        assertEquals(expectedUser, recoveredUser);
    }

    @Test
    void deactivateMultipleUsers_ValidIds_ReturnsTrue() throws DAOException {
        boolean result = userDAO.deactivateMultipleUsers(List.of(STORED_ADMINISTRATOR_ID, STORED_COORDINATOR_ID));

        assertTrue(result);
    }

    @Test
    void insertUser_DuplicateUsername_ThrowsDAOException() throws SQLException {
        newUser.setUserName(STORED_ADMIN_USERNAME);

        try (Connection connection = dbConnection.getConnection()) {
            assertThrows(DAOException.class, () -> userDAO.insertUser(newUser, connection));
        }
    }

    @Test
    void verifyCredentialsByUserName_InvalidPassword_ReturnsFalse() throws DAOException {
        boolean result = userDAO.verifyCredentialsByUserName(STORED_ADMIN_USERNAME, "ClaveIncorrecta99");

        assertFalse(result);
    }

    @Test
    void getUserByUserName_NonExistentUser_ReturnsEmptyObject() throws DAOException {
        User recoveredUser = userDAO.getUserByUserName("usuarioInexistente");

        assertEquals(new User(), recoveredUser);
    }
}
