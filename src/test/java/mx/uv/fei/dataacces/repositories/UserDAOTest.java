package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.dataacces.exceptions.DAOException;

public class UserDAOTest {
    private UserDAO userDAO;
    private User testUser;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();

        testUser = new User();
        testUser.setName("Angel");
        testUser.setLastName("Aguilar");
        testUser.setPassword("password123");
        testUser.setStatus("activo");
        testUser.setGender("Masculino");
    }

    @Test
    void testInsertUserSuccess() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            int id = userDAO.insertUser(testUser, conn);
            assertTrue(id > 0, "El ID generado debería ser mayor a 0");
        } catch (SQLException | DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testDeactivateUserSuccess() {
        int idGenerado = -1;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            idGenerado = userDAO.insertUser(testUser, conn);
          
            boolean result = userDAO.deactivateUser(idGenerado);
            assertTrue(result, "La desactivación debería devolver true");
            
        } catch (SQLException | DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }
}