package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;

import mx.uv.fei.domain.dto.User;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

@SpringBootTest
@ActiveProfiles("test")
public class UserDAOTest {

    private IDatabaseConnection dbconnection;
    private UserDAO userDAO;
    private User testUser;

    @Autowired
    public UserDAOTest(IDatabaseConnection dbconnection, UserDAO userDAO, User testUser) {
        this.dbconnection = dbconnection;
        this.userDAO = userDAO;
        this.testUser = testUser;
    }

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
        try (Connection conn = dbconnection.getConnection()) {
            int id = userDAO.insertUser(testUser, conn);
            assertTrue(id > 0, "El ID generado debería ser mayor a 0");
        } catch (SQLException | DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testDeactivateUserSuccess() {
        int idGenerado = -1;

        try (Connection conn = dbconnection.getConnection()) {
            idGenerado = userDAO.insertUser(testUser, conn);

            boolean result = userDAO.deactivateUser(idGenerado);
            assertTrue(result, "La desactivación debería devolver true");

        } catch (SQLException | DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }
}