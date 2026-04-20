package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDateTime;

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

import mx.uv.fei.domain.dto.AuthenticationToken;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class AuthenticationTokenDAOIT {
    @Autowired
    private AuthenticationTokenDAO tokenDAO;

    private AuthenticationToken expectedToken;

    @BeforeEach
    void setUp() {
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
