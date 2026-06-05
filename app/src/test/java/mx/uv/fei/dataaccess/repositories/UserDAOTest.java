package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;

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
        assertNotNull(dbConnection);
        assertNotNull(userDAO);

        TestDatabaseSetup.initialize(dbConnection);

        testUser = new User();
        testUser.setName("Angel");
        testUser.setLastName("Aguilar");
        testUser.setPassword("password123");
        testUser.setStatus(UserStatus.valueOf("activo"));
        testUser.setGender(Gender.valueOf("Masculino"));
    }

    @Test
    void insertUser_ValidUser_ReturnsGeneratedId() throws SQLException, DAOException {
        try (Connection conn = dbConnection.getConnection()) {

            int generatedId = userDAO.insertUser(testUser, conn);

            assertTrue(generatedId > 0, "El usuario debió insertarse y generar un ID mayor a cero.");
        }
    }

    @Test
    void deactivateUser_ExistingId_ReturnsTrue() throws SQLException, DAOException {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAO.insertUser(testUser, conn);

            boolean result = userDAO.deactivateUser(generatedId);

            assertTrue(result, "La desactivación del usuario debió retornar true.");
        }
    }

    @Test
    void updateUser_ValidModifiedData_ReturnsTrue() throws SQLException, DAOException {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAO.insertUser(testUser, conn);
            testUser.setId(generatedId);
            testUser.setStatus(UserStatus.valueOf("no activo"));
            testUser.setPassword("newpassword456");

            boolean result = userDAO.updateUser(testUser, conn);

            assertTrue(result, "La actualización del usuario debió retornar true.");
        }
    }
}