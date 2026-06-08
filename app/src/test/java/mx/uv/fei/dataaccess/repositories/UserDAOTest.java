package mx.uv.fei.dataaccess.repositories;

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

import static org.junit.jupiter.api.Assertions.*;

@StartEtiquetteTest
@Profile("test")
public class UserDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IUserDAO userDAO;

    private User testUser;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testUser = new User();
        testUser.setUserName("userTest01");
        testUser.setEmail("userTest01@uv.mx");
        testUser.setName("Angel");
        testUser.setLastName("Aguilar");
        testUser.setPassword("password123");
        testUser.setRole("Practitioner");
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setGender(Gender.MALE);
    }

    @Test
    void insertUser_ValidUser_ReturnsGeneratedId() throws SQLException, DAOException {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAO.insertUser(testUser, conn);
            assertTrue(generatedId > 0);
        }
    }

    @Test
    void deactivateUser_ExistingId_ReturnsTrue() throws DAOException {
        boolean result = userDAO.deactivateUser(13);
        assertTrue(result);
    }

    @Test
    void updateUser_ValidModifiedData_ReturnsTrue() throws SQLException, DAOException {
        try (Connection conn = dbConnection.getConnection()) {
            testUser.setId(13);
            testUser.setStatus(UserStatus.INACTIVE);
            testUser.setPassword("newPassword456");

            boolean result = userDAO.updateUser(testUser, conn);
            assertTrue(result);
        }
    }

    @Test
    void verifyCredentialsByUserName_ValidCredentials_ReturnsTrue() throws DAOException {
        boolean result = userDAO.verifyCredentialsByUserName("12345", "12345");
        assertTrue(result);
    }

    @Test
    void verifyCredentialsByEmail_ValidCredentials_ReturnsTrue() throws DAOException {
        boolean result = userDAO.verifyCredentialsByEmail("adm@adm.com", "12345");
        assertTrue(result);
    }

    @Test
    void getUserRole_ExistingUser_ReturnsRole() throws DAOException {
        String role = userDAO.getUserRole("12345");
        assertEquals("Administrator", role);
    }

    @Test
    void getUserByUserName_ExistingUser_ReturnsUser() throws DAOException {
        User expectedUser = new User();
        expectedUser.setId(13);
        expectedUser.setUserName("12345");
        expectedUser.setPassword("12345");
        expectedUser.setName("adm");
        expectedUser.setLastName("adm");
        expectedUser.setEmail("adm@adm.com");
        expectedUser.setRole("Administrator");
        expectedUser.setStatus(UserStatus.ACTIVE);
        expectedUser.setGender(Gender.MALE);

        User recovered = userDAO.getUserByUserName("12345");
        assertEquals(expectedUser, recovered);
    }

    @Test
    void getUserByEmail_ExistingUser_ReturnsUser() throws DAOException {
        User expectedUser = new User();
        expectedUser.setId(13);
        expectedUser.setUserName("12345");
        expectedUser.setPassword("12345");
        expectedUser.setName("adm");
        expectedUser.setLastName("adm");
        expectedUser.setEmail("adm@adm.com");
        expectedUser.setRole("Administrator");
        expectedUser.setStatus(UserStatus.ACTIVE);
        expectedUser.setGender(Gender.MALE);

        User recovered = userDAO.getUserByEmail("adm@adm.com");
        assertEquals(expectedUser, recovered);
    }

    @Test
    void deactivateMultipleUsers_ValidIds_ReturnsTrue() throws DAOException {
        boolean result = userDAO.deactivateMultipleUsers(List.of(13, 67));
        assertTrue(result);
    }

    @Test
    void insertUser_DuplicateUsername_ThrowsDAOException() throws SQLException {
        testUser.setUserName("12345");
        try (Connection conn = dbConnection.getConnection()) {
            assertThrows(DAOException.class, () -> {
                userDAO.insertUser(testUser, conn);
            });
        }
    }

    @Test
    void verifyCredentialsByUserName_InvalidPassword_ReturnsFalse() throws DAOException {
        boolean result = userDAO.verifyCredentialsByUserName("12345", "wrongpass");
        assertFalse(result);
    }

    @Test
    void getUserByUserName_NonExistentUser_ReturnsNull() throws DAOException {
        User recovered = userDAO.getUserByUserName("fantasma");
        assertNull(recovered);
    }
}