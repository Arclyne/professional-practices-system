package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.Connection;
import java.sql.SQLException;

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
public class UserDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IUserDAO userDAOTest;

    private User testUser;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection);
        assertNotNull(userDAOTest);

        TestDatabaseSetup.initialize(dbConnection);

        testUser = new User();
        testUser.setName("Angel");
        testUser.setLastName("Aguilar");
        testUser.setPassword("password123");
        testUser.setStatus("activo");
        testUser.setGender("Masculino");
    }

    @Test
    void testInsertUserSuccess() throws SQLException, DAOException {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAOTest.insertUser(testUser, conn);
            assertTrue(generatedId > 0);
        }
    }

    @Test
    void testDeactivateUserSuccess() throws SQLException, DAOException {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAOTest.insertUser(testUser, conn);
            boolean result = userDAOTest.deactivateUser(generatedId);
            assertTrue(result);
        }
    }

    @Test
    void testUpdateUserSuccess() throws SQLException, DAOException {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAOTest.insertUser(testUser, conn);

            testUser.setId(generatedId);
            testUser.setStatus("no activo");
            testUser.setPassword("newpassword456");

            boolean result = userDAOTest.updateUser(testUser, conn);
            assertTrue(result);
        }
    }
}