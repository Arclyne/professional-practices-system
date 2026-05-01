package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IAuthenticationToken;
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
        // Obtenemos el usuario activo mediante el Facade
        User currentUser = session.getCurrentUser();

        if (currentUser == null) {
            throw new ManagerException("No hay una sesión activa para generar el token.");
        }

        SecureRandom secureRandom = new SecureRandom();
        int newToken = 100000 + secureRandom.nextInt(900000);

        AuthenticationToken token = new AuthenticationToken();
        token.setUserName(currentUser.getUserName());
        token.setValueToken(newToken);
        token.setTimeCreation(LocalDateTime.now());

        try {
            tokenDAO.insertToken(token);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema. Por favor, intente más tarde.", e);
        }
    }

    public void verifyToken(String tokenInput) throws ManagerException {
        if (tokenInput == null || tokenInput.trim().isEmpty()) {
            throw new ManagerException("Por favor, ingrese el código de 6 dígitos.");
        }

        int parsedToken;
        try {
            parsedToken = Integer.parseInt(tokenInput.trim());
        } catch (NumberFormatException e) {
            throw new ManagerException("El código debe contener únicamente números.");
        }

        User currentUser = session.getCurrentUser();
        if (currentUser == null) {
            throw new ManagerException("No hay una sesión activa para verificar el token.");
        }

        try {
            LocalDateTime tokenCreationTime = tokenDAO.getTokenCreationTime(parsedToken, currentUser.getUserName());

            if (tokenCreationTime == null) {
                throw new ManagerException("El token ingresado es incorrecto.");
            }

            long secondsElapsed = ChronoUnit.SECONDS.between(tokenCreationTime, LocalDateTime.now());

            if (secondsElapsed > 60) {
                throw new ManagerException("El token ha expirado (han pasado más de 60 segundos). Solicite uno nuevo.");
            }

        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al verificar el token en la base de datos.", e);
        }
    }
}