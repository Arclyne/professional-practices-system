package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.time.LocalDateTime;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IAuthenticationToken;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.AuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")

public class AuthenticationTokenDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IAuthenticationToken authenticationTokenDAO;

    private AuthenticationToken validToken;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        validToken = new AuthenticationToken();
        validToken.setValueToken(987654);
        validToken.setTimeCreation(LocalDateTime.now().withNano(0));
        validToken.setUserName("zS24242424");
    }

    @Test
    void insertToken_ValidToken_ReturnsTrue() throws DAOException {
        boolean result = authenticationTokenDAO.insertToken(validToken);

        assertTrue(result, "La inserción del token debería retornar true");
    }

    @Test
    void recoverToken_ExistingToken_ReturnsTokenObject() throws DAOException {
        AuthenticationToken expectedToken = new AuthenticationToken();
        expectedToken.setValueToken(123456);
        expectedToken.setUserName("test");

        AuthenticationToken recovered = authenticationTokenDAO.recoverToken(123456);

        assertEquals(expectedToken, recovered, "El token recuperado debe coincidir exactamente con el esperado");
    }

    @Test
    void getTokenCreationTime_ValidTokenAndUser_ReturnsLocalDateTime() throws DAOException {
        LocalDateTime creationTime = authenticationTokenDAO.getTokenCreationTime(123456, "test");

        assertNotNull(creationTime, "El tiempo de creación no debería ser nulo para un token y usuario válidos");
    }

    @Test
    void insertToken_DuplicateTokenValue_ThrowsDAOException() {
        AuthenticationToken duplicateToken = new AuthenticationToken();
        duplicateToken.setValueToken(123456);
        duplicateToken.setTimeCreation(LocalDateTime.now().withNano(0));
        duplicateToken.setUserName("zS24242424");

        assertThrows(DAOException.class, () -> {
            authenticationTokenDAO.insertToken(duplicateToken);
        }, "Debería lanzar DAOException al intentar insertar un valor de token duplicado (Primary Key)");
    }

    @Test
    void insertToken_NonExistentUser_ThrowsDAOException() {
        AuthenticationToken orphanToken = new AuthenticationToken();
        orphanToken.setValueToken(111222);
        orphanToken.setTimeCreation(LocalDateTime.now().withNano(0));
        orphanToken.setUserName("usuarioFantasma");

        assertThrows(DAOException.class, () -> {
            authenticationTokenDAO.insertToken(orphanToken);
        }, "Debería lanzar DAOException al intentar insertar un token para un username inexistente (Foreign Key)");
    }

    @Test
    void recoverToken_NonExistentToken_ReturnsNull() throws DAOException {
        int nonExistentTokenValue = 999999;

        AuthenticationToken recovered = authenticationTokenDAO.recoverToken(nonExistentTokenValue);
        assertNull(recovered, "Debería retornar nulo si el token no existe en la base de datos");
    }

    @Test
    void getTokenCreationTime_InvalidUserForToken_ReturnsNull() throws DAOException {
        int validTokenValue = 123456;
        String invalidUserForThisToken = "zS24242424";

        LocalDateTime creationTime = authenticationTokenDAO.getTokenCreationTime(validTokenValue, invalidUserForThisToken);
        assertNull(creationTime, "Debería retornar nulo si la combinación de token y usuario no es exacta");
    }
}
