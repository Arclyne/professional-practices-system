package mx.uv.fei.dataacces.repositories;


import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
    void testInsertUserSuccess() throws DAOException {
        int id = userDAO.insertUser(testUser);
    
        assertTrue(id > 0);
    }

    @Test
    void testDeactivateUserSuccess() throws DAOException {
        int id = userDAO.insertUser(testUser);
        
        boolean result = userDAO.deactivateUser(id);
        
        assertTrue(result);
    }
}