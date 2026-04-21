package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.AuthenticationToken;

public class AuthenticationTokenDAOIT {

    private IDatabaseConnection dbConnection;
    private DatabasePropeties propeties;
    private AuthenticationTokenDAO tokenDAO;

    private AuthenticationToken expectedToken;

    @BeforeEach
    void setUp() {
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        tokenDAO = new AuthenticationTokenDAO(dbConnection);
        expectedToken = new AuthenticationToken();
        expectedToken.setUserId(1);
        expectedToken.setValueToken(123456);
    }

    @Test
    void testInsertTokenSuccess() {

        AuthenticationToken token = new AuthenticationToken();
        token.setValueToken(12345);
        token.setUserId(1);
        token.setTimeCreation(LocalDateTime.now());
        try {
            boolean result = tokenDAO.insertToken(token);
            assertTrue(result);
        } catch (DAOException e) {
            fail("Test failed: " + e.getMessage());
        }

    }

    @Test
    void testSelectTokenNullSuccess() {
        try {
            AuthenticationToken tokenTest = tokenDAO.recoverToken(999999);
            assertNull(tokenTest);
        } catch (DAOException e) {
            fail("La prueba falló. MySQL dice: " + e);
        }
    }

    @Test
    void testSelectTokenSucces() {
        try {
            AuthenticationToken tokenTest = tokenDAO.recoverToken(123456);
            assertNotNull(tokenTest);

            assertEquals(expectedToken, tokenTest);
        } catch (DAOException e) {
            fail("La prueba falló. MySQL dice: " + e);
        }
    }
}
