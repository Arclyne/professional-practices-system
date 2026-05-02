package mx.uv.fei.dataacces.repositories;

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
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.AuthenticationToken;

@StartEtiquetteTest
@Profile("test")
public class AuthenticationTokenDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private AuthenticationTokenDAO tokenDAO;

    private AuthenticationToken expectedToken;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection, "La conexión a la base de datos no fue inyectada.");
        assertNotNull(tokenDAO, "El DAO no fue inyectado correctamente.");

        TestDatabaseSetup.initialize(dbConnection);

        expectedToken = new AuthenticationToken();
        expectedToken.setUserName("Test");
        expectedToken.setValueToken(123456);
    }

    @Test
    void testInsertTokenSuccess() throws DAOException {
        AuthenticationToken token = new AuthenticationToken();
        token.setValueToken(12345);
        token.setUserName("test");
        token.setTimeCreation(LocalDateTime.now());

        boolean result = tokenDAO.insertToken(token);
        assertTrue(result, "El token debería haberse insertado con éxito.");
    }

    @Test
    void testSelectTokenNullSuccess() throws DAOException {

        AuthenticationToken tokenTest = tokenDAO.recoverToken(999999);
        assertNull(tokenTest, "Se esperaba null para un token inexistente.");
    }

    @Test
    void testSelectTokenSucces() throws DAOException {
        AuthenticationToken tokenTest = tokenDAO.recoverToken(123456);
        assertEquals(expectedToken, tokenTest, "El token recuperado no coincide con el esperado.");
    }
}