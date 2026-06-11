package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    private static final int STORED_TOKEN_VALUE = 123456;
    private static final int NEW_TOKEN_VALUE = 987654;
    private static final String STORED_TOKEN_OWNER = "zS23151617";
    private static final String PRACTITIONER_USERNAME = "zS24242424";

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IAuthenticationToken authenticationTokenDAO;

    private AuthenticationToken validToken;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        validToken = new AuthenticationToken();
        validToken.setValueToken(NEW_TOKEN_VALUE);
        validToken.setTimeCreation(LocalDateTime.now().withNano(0));
        validToken.setUserName(PRACTITIONER_USERNAME);
    }

    @Test
    void insertToken_ValidToken_ReturnsTrue() throws DAOException {
        boolean result = authenticationTokenDAO.insertToken(validToken);

        assertTrue(result, "La insercion del token deberia retornar true");
    }

    @Test
    void recoverToken_ExistingToken_ReturnsTokenObject() throws DAOException {
        AuthenticationToken expectedToken = new AuthenticationToken();
        expectedToken.setValueToken(STORED_TOKEN_VALUE);
        expectedToken.setUserName(STORED_TOKEN_OWNER);

        AuthenticationToken recoveredToken = authenticationTokenDAO.recoverToken(STORED_TOKEN_VALUE);

        assertEquals(expectedToken, recoveredToken, "El token recuperado debe coincidir exactamente con el esperado");
    }

    @Test
    void getTokenCreationTime_ValidTokenAndUser_ReturnsLocalDateTime() throws DAOException {
        authenticationTokenDAO.insertToken(validToken);

        LocalDateTime creationTime = authenticationTokenDAO.getTokenCreationTime(NEW_TOKEN_VALUE, PRACTITIONER_USERNAME);

        assertEquals(validToken.getTimeCreation(), creationTime, "El tiempo de creacion debe coincidir exactamente");
    }

    @Test
    void insertToken_DuplicateTokenValue_ThrowsDAOException() {
        AuthenticationToken duplicateToken = new AuthenticationToken();
        duplicateToken.setValueToken(STORED_TOKEN_VALUE);
        duplicateToken.setTimeCreation(LocalDateTime.now().withNano(0));
        duplicateToken.setUserName(PRACTITIONER_USERNAME);

        assertThrows(DAOException.class, () -> authenticationTokenDAO.insertToken(duplicateToken));
    }

    @Test
    void insertToken_NonExistentUser_ThrowsDAOException() {
        AuthenticationToken orphanToken = new AuthenticationToken();
        orphanToken.setValueToken(111222);
        orphanToken.setTimeCreation(LocalDateTime.now().withNano(0));
        orphanToken.setUserName("usuarioInexistente");

        assertThrows(DAOException.class, () -> authenticationTokenDAO.insertToken(orphanToken));
    }

    @Test
    void recoverToken_NonExistentToken_ReturnsEmptyObject() throws DAOException {
        AuthenticationToken recoveredToken = authenticationTokenDAO.recoverToken(999999);

        assertEquals(new AuthenticationToken(), recoveredToken, "Deberia retornar objeto vacio si el token no existe");
    }

    @Test
    void getTokenCreationTime_InvalidUserForToken_ReturnsNull() throws DAOException {
        LocalDateTime creationTime = authenticationTokenDAO.getTokenCreationTime(STORED_TOKEN_VALUE, PRACTITIONER_USERNAME);

        assertNull(creationTime);
    }
}
