package mx.uv.fei.domain.manager.identity;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IAuthenticationToken;
import mx.uv.fei.domain.dto.AuthenticationToken;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.statemachine.SessionFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);

    private static final int TOKEN_MINIMUM_VALUE = 100000;
    private static final int TOKEN_RANDOM_BOUND = 900000;
    private static final int TOKEN_EXPIRATION_SECONDS = 60;

    private final IAuthenticationToken tokenDAO;
    private final SessionFacade session;

    @Inject
    public TokenManager(IAuthenticationToken tokenDAO, SessionFacade session) {
        this.tokenDAO = tokenDAO;
        this.session = session;
    }

    public void generateToken() throws ManagerException {
        User currentUser = session.getCurrentUser();
        if (currentUser == null) {
            throw new ManagerException("No hay una sesión de usuario activa para generar el token.");
        }

        int generatedTokenValue = TOKEN_MINIMUM_VALUE + new SecureRandom().nextInt(TOKEN_RANDOM_BOUND);

        AuthenticationToken token = new AuthenticationToken();
        token.setUserName(currentUser.getUserName());
        token.setValueToken(generatedTokenValue);
        token.setTimeCreation(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        try {
            tokenDAO.insertToken(token);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al registrar el token para el usuario.", e);
        }
    }

    public void verifyToken(String tokenInput) throws ManagerException {
        if (tokenInput == null || tokenInput.trim().isEmpty()) {
            throw new ManagerException("El código de token no puede estar vacío.");
        }

        int parsedTokenValue = parseTokenValue(tokenInput);
        User currentUser = session.getCurrentUser();

        if (currentUser == null) {
            throw new ManagerException("No hay una sesión de usuario activa para verificar el token.");
        }

        try {
            LocalDateTime tokenCreationTime = tokenDAO.getTokenCreationTime(parsedTokenValue, currentUser.getUserName());

            if (tokenCreationTime == null) {
                throw new ManagerException("El token ingresado no es válido. Verifique el código e intente nuevamente.");
            }

            long elapsedSeconds = ChronoUnit.SECONDS.between(tokenCreationTime, LocalDateTime.now());
            if (elapsedSeconds > TOKEN_EXPIRATION_SECONDS) {
                throw new ManagerException("El token ha expirado. Solicite un nuevo código.");
            }
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error de conexión durante la verificación del token.", e);
        }
    }

    private int parseTokenValue(String tokenInput) throws ManagerException {
        try {
            return Integer.parseInt(tokenInput.trim());
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("El código de token debe ser un valor numérico.", e);
        }
    }
}