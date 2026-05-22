package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IAuthenticationToken;
import mx.uv.fei.domain.dto.AuthenticationToken;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.SessionFacade;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class TokenManager {

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
            throw new ManagerException("Violación de estado: Intento de generar token sin una sesión de usuario activa en el Store.");
        }

        SecureRandom secureRandom = new SecureRandom();
        int newToken = 100000 + secureRandom.nextInt(900000);

        AuthenticationToken token = new AuthenticationToken();
        token.setUserName(currentUser.getUserName());
        token.setValueToken(newToken);
        token.setTimeCreation(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));

        try {
            tokenDAO.insertToken(token);
        } catch (DAOException e) {
            throw new ManagerException("Error de persistencia al intentar registrar el nuevo token para el usuario '" + currentUser.getUserName() + "': " + e.getMessage(), e);
        }
    }

    public void verifyToken(String tokenInput) throws ManagerException {
        if (tokenInput == null || tokenInput.trim().isEmpty()) {
            throw new ManagerException("Validación fallida: El campo de código de token se recibió vacío o nulo.");
        }

        int parsedToken;
        try {
            parsedToken = Integer.parseInt(tokenInput.trim());
        } catch (NumberFormatException e) {
            throw new ManagerException(String.format("Validación de formato fallida: Se esperaba un código numérico, pero se recibió '%s'.", tokenInput), e);
        }

        User currentUser = session.getCurrentUser();
        if (currentUser == null) {
            throw new ManagerException("Violación de estado: Intento de verificar un token sin una sesión de usuario activa en el Store.");
        }

        try {
            LocalDateTime tokenCreationTime = tokenDAO.getTokenCreationTime(parsedToken, currentUser.getUserName());

            if (tokenCreationTime == null) {
                throw new ManagerException(String.format("Autenticación denegada: El token [%d] no coincide con los registros del usuario '%s'.", parsedToken, currentUser.getUserName()));
            }

            long secondsElapsed = ChronoUnit.SECONDS.between(tokenCreationTime, LocalDateTime.now());

            if (secondsElapsed > 60) {
                throw new ManagerException(String.format("Token expirado: El token [%d] del usuario '%s' expiró tras %d segundos de vida útil.", parsedToken, currentUser.getUserName(), secondsElapsed));
            }

        } catch (DAOException e) {
            throw new ManagerException("Error de base de datos durante el proceso de verificación del token: " + e.getMessage(), e);
        }
    }
}