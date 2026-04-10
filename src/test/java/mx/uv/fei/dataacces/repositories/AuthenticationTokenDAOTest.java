package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.AuthenticationToken;

public class AuthenticationTokenDAOTest {

    @Test
    void testInsertToken() {

        AuthenticationTokenDAO tokenDAO = new AuthenticationTokenDAO();
        AuthenticationToken token = new AuthenticationToken();
        token.setValueToken(12345);
        token.setUserId(1);
        try {
            boolean result = tokenDAO.insertToken(token);
            assertTrue(result);
        } catch (DAOException e) {
            fail("La prueba falló. MySQL dice: " + e);
        }

    }

    @Test
    void testRecoverToken() {
        AuthenticationTokenDAO tokenDAO = new AuthenticationTokenDAO();
        try {
            AuthenticationToken tokenTest = tokenDAO.recoverToken(12345);
            assertNotNull(tokenTest);
        } catch (DAOException e) {
            fail("La prueba falló. MySQL dice: " + e);
        }
    }
}
