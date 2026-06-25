package mx.uv.fei.domain.manager.identity;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.LoginMethod;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.AuthenticatorAction;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Pattern;

@Component
public class StartSessionManager {

    private static final Logger log = LoggerFactory.getLogger(StartSessionManager.class);
    private static final String ROLE_PRACTITIONER = "Practitioner";
    private static final String GENERIC_AUTH_ERROR = "Credenciales incorrectas. Verifique su información.";
    private static final String GENERIC_SYSTEM_ERROR = "Ocurrió un error al procesar la solicitud. Por favor, intente más tarde.";

    private final IUserDAO userDAO;
    private final AppStore store;

    @Inject
    public StartSessionManager(IUserDAO userDAO, AppStore store) {
        this.userDAO = userDAO;
        this.store = store;
    }

    public void processLogin(Map<String, String> credential) throws ManagerException {
        String identifier = credential.get("Identifier");
        String password = credential.get("Password");
        LoginMethod loginMethod = determineLoginMethod(identifier);

        try {
            User authenticatedUser = authenticateAndRetrieveUser(identifier, password, loginMethod);
            validateRoleAccess(authenticatedUser, loginMethod);
            store.dispatch(new AuthenticatorAction.LoginSuccess(authenticatedUser));
            routeUserByStatus(authenticatedUser);
        } catch (DAOException e) {
            log.error("Error de base de datos al iniciar sesión.", e);
            throw new ManagerException(GENERIC_SYSTEM_ERROR, e);
        }
    }

    private LoginMethod determineLoginMethod(String identifier) throws ManagerException {
        if (BaseValidator.isValidEnrollment(identifier)) {
            return LoginMethod.ENROLLMENT;
        } else if (BaseValidator.isValidEmail(identifier)) {
            return LoginMethod.EMAIL;
        } else {
            throw new ManagerException(GENERIC_AUTH_ERROR);
        }
    }

    private User authenticateAndRetrieveUser(String identifier, String password, LoginMethod loginMethod) throws DAOException, ManagerException {
        boolean isCredentialValid;
        User user;

        if (loginMethod == LoginMethod.EMAIL) {
            isCredentialValid = userDAO.verifyCredentialsByEmail(identifier, password);
            user = isCredentialValid ? userDAO.getUserByEmail(identifier) : null;
        } else {
            isCredentialValid = userDAO.verifyCredentialsByUserName(identifier, password);
            user = isCredentialValid ? userDAO.getUserByUserName(identifier) : null;
        }

        if (!isCredentialValid || user == null) {
            throw new ManagerException(GENERIC_AUTH_ERROR);
        }

        return user;
    }

    private void validateRoleAccess(User user, LoginMethod loginMethod) throws ManagerException {
        boolean isPractitioner = user.getRole().equalsIgnoreCase(ROLE_PRACTITIONER);

        if ((isPractitioner && loginMethod == LoginMethod.EMAIL) ||
                (!isPractitioner && loginMethod == LoginMethod.ENROLLMENT)) {
            throw new ManagerException(GENERIC_AUTH_ERROR);
        }
    }

    private void routeUserByStatus(User user) throws ManagerException {
        switch (user.getStatus()) {
            case PENDING:
                store.dispatch(new NavigationAction.GoToSection(AppSection.PASSWORD_RESET));
                break;
            case ACTIVE:
                store.dispatch(new NavigationAction.GoToSection(AppSection.TOKEN_VERIFICATION));
                break;
            case INACTIVE:
            default:
                throw new ManagerException(GENERIC_AUTH_ERROR);
        }
    }
}