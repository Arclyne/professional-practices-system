package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import mx.uv.fei.TestApp;
import mx.uv.fei.TestConfig;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class UserDAOIT {
    @Autowired
    private IDatabaseConnection dbConnection;

    @Autowired
    private IUserDAO userDAOTest;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setName("Angel");
        testUser.setLastName("Aguilar");
        testUser.setPassword("password123");
        testUser.setStatus("activo");
        testUser.setGender("Masculino");
    }

    @Test
    void testInsertUserSuccess() {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAOTest.insertUser(testUser, conn);
            assertTrue(generatedId > 0, "El ID generado debería ser mayor a 0");
        } catch (SQLException | DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testDeactivateUserSuccess() {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAOTest.insertUser(testUser, conn);

            boolean result = userDAOTest.deactivateUser(generatedId);
            assertTrue(result, "La desactivación debería devolver true");

        } catch (SQLException | DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testUpdateUserSuccess() {
        try (Connection conn = dbConnection.getConnection()) {
            int generatedId = userDAOTest.insertUser(testUser, conn);
    
            testUser.setId(generatedId);
            testUser.setStatus("no activo"); 
            testUser.setPassword("newpassword456");

            boolean result = userDAOTest.updateUser(testUser, conn);
            assertTrue(result, "La actualización debería devolver true");

        } catch (SQLException | DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }
}