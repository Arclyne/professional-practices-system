package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.enums.LoginMethod;
import mx.uv.fei.domain.dto.User;
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

    private static final Logger logger = LoggerFactory.getLogger(StartSessionManager.class);
    private final IUserDAO userDAO;
    private final AppStore store;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern ENROLLMENT_PATTERN = Pattern.compile("^(zs)[0-9]{8}$", Pattern.CASE_INSENSITIVE);

    @Inject
    public StartSessionManager(IUserDAO userDAO, AppStore store) {
        this.userDAO = userDAO;
        this.store = store;
    }

    public void handleActionConnectButton(Map<String, String> credential) throws ManagerException {
        String identifier = credential.get("Identifier");
        String password = credential.get("Password");

        LoginMethod loginMethod = determineLoginMethod(identifier);

        try {
            User retrievedUser = authenticateAndRetrieveUser(identifier, password, loginMethod);
            validateRoleAccess(retrievedUser, loginMethod);
            store.dispatch(new AuthenticatorAction.LoginSuccess(retrievedUser));

            routeUserByStatus(retrievedUser);

        } catch (DAOException e) {
            logger.error("Error de conexión al iniciar sesión", e);
            throw new ManagerException("Ocurrió un problem de conexión con el servidor. Por favor, intente más tarde.", e);
        }
    }

    private LoginMethod determineLoginMethod(String identifier) throws ManagerException {
        if (ENROLLMENT_PATTERN.matcher(identifier).matches()) {
            return LoginMethod.ENROLLMENT;
        } else if (EMAIL_PATTERN.matcher(identifier).matches()) {
            return LoginMethod.EMAIL;
        } else {
            throw new ManagerException("El identificador no tiene un formato válido. Ingrese un correo electrónico o una matrícula institucional.");
        }
    }

    private User authenticateAndRetrieveUser(String identifier, String password, LoginMethod method) throws DAOException, ManagerException {
        boolean isValidCredentials;
        User user;

        if (method == LoginMethod.EMAIL) {
            isValidCredentials = userDAO.verifyCredentialsByEmail(identifier, password);
            user = isValidCredentials ? userDAO.getUserByEmail(identifier) : null;
        } else {
            isValidCredentials = userDAO.verifyCredentialsByUserName(identifier, password);
            user = isValidCredentials ? userDAO.getUserByUserName(identifier) : null;
        }

        if (!isValidCredentials || user == null) {
            throw new ManagerException("Credenciales incorrectas. Verifique su información.");
        }

        return user;
    }

    private void validateRoleAccess(User user, LoginMethod method) throws ManagerException {
        String role = user.getRole();

        if (role.equalsIgnoreCase("Practitioner") && method == LoginMethod.EMAIL) {
            throw new ManagerException("Acceso denegado: Los practicantes deben iniciar sesión exclusivamente con su Matrícula.");
        } else if (!role.equalsIgnoreCase("Practitioner") && method == LoginMethod.ENROLLMENT) {
            throw new ManagerException("Acceso denegado: El personal administrativo y académico debe iniciar sesión con su Correo Electrónico.");
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
                throw new ManagerException("Usuario inactivo. Comuníquese con su Coordinador.");
            default:
                throw new ManagerException("Estado de usuario no reconocido en el sistema.");
        }
    }
}
