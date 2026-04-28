package mx.uv.fei.domain.manager;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IAuthenticationToken;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.repositories.AuthenticationTokenDAO;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TokenManager {

    private final IAuthenticationToken tokenDAO;

    public TokenManager(IDatabaseConnection databaseConnection) {
        this.tokenDAO = new AuthenticationTokenDAO(databaseConnection);
    }

    public int generateToken() {
        SecureRandom secureRandom = new SecureRandom();
        return 100000 + secureRandom.nextInt(900000);
    }

    public void verifyToken(String tokenInput, int userId) throws ManagerException {
        if (tokenInput == null || tokenInput.trim().isEmpty()) {
            throw new ManagerException("Por favor, ingrese el código de 6 dígitos.");
        }

        int parsedToken;
        try {
            parsedToken = Integer.parseInt(tokenInput.trim());
        } catch (NumberFormatException e) {
            throw new ManagerException("El código debe contener únicamente números.");
        }

        try {
            LocalDateTime tokenCreationTime = tokenDAO.getTokenCreationTime(parsedToken, userId);

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