package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IAuthenticationToken;
import mx.uv.fei.domain.dto.AuthenticationToken;

@StartEtiquetteTest
@Profile("test")
public class AuthenticationTokenDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IAuthenticationToken tokenDAO;

    private AuthenticationToken expectedToken;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection);
        assertNotNull(tokenDAO);
        TestDatabaseSetup.initialize(dbConnection);
        expectedToken = new AuthenticationToken();
        expectedToken.setUserName("test");
        expectedToken.setValueToken(123456);
    }

    @Test
    void insertToken_ValidToken_ReturnsTrue() throws DAOException {
        AuthenticationToken token = new AuthenticationToken();
        token.setValueToken(12345);
        token.setUserName("test");
        token.setTimeCreation(LocalDateTime.now());

        boolean result = tokenDAO.insertToken(token);

        assertTrue(result);
    }

    @Test
    void recoverToken_NonExistingToken_ReturnsNull() throws DAOException {

        AuthenticationToken tokenTest = tokenDAO.recoverToken(999999);

        assertNull(tokenTest);
    }

    @Test
    void recoverToken_ExistingToken_ReturnsToken() throws DAOException {

        AuthenticationToken tokenTest = tokenDAO.recoverToken(123456);

        assertEquals(expectedToken, tokenTest);
    }
}