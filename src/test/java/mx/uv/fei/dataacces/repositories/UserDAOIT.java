package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;

public class UserDAOIT {

    private DatabasePropeties propeties;
    private IDatabaseConnection dbConnection;
    private IUserDAO userDAOTest;

    private User testUser;

    @BeforeEach
    void setUp() throws SQLException {
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        TestDatabaseSetup.initialize(dbConnection);

        userDAOTest = new UserDAO(dbConnection);
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

            assertTrue(generatedId > 0, "El ID generado debería ser mayor a 0");
        }
    }

    @Test
    void testDeactivateUserSuccess() throws SQLException, DAOException {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAOTest.insertUser(testUser, conn);

            boolean result = userDAOTest.deactivateUser(generatedId);

            assertTrue(result, "La desactivación debería devolver true");
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

            assertTrue(result, "La actualización debería devolver true");
        }
    }
}