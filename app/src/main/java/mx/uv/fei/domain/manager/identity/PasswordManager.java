package mx.uv.fei.domain.manager.identity;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.common.validators.PasswordValidator;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.SessionFacade;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class PasswordManager {

    private static final Logger log = LoggerFactory.getLogger(PasswordManager.class);

    private static final String TEMPORARY_PASSWORD_PREFIX = "Temp-";
    private static final int TEMPORARY_PASSWORD_SUFFIX_LENGTH = 8;

    private final IUserDAO userDAO;
    private final IDatabaseConnection databaseConnection;
    private final SessionFacade session;
    private final AppStore store;

    @Inject
    public PasswordManager(IUserDAO userDAO, IDatabaseConnection databaseConnection, SessionFacade session, AppStore store) {
        this.userDAO = userDAO;
        this.databaseConnection = databaseConnection;
        this.session = session;
        this.store = store;
    }

    public void updatePasswordAndActivate(String newPassword, String confirmPassword) throws ManagerException {
        User currentUser = session.getCurrentUser();
        if (currentUser == null) {
            throw new ManagerException("No se encontró una sesión activa para actualizar.");
        }

        PasswordValidator.validatePassword(confirmPassword);
        currentUser.setStatus(UserStatus.ACTIVE);
        currentUser.setPassword(newPassword);

        try (Connection sharedConnection = databaseConnection.getConnection()) {
            userDAO.updateUser(currentUser, sharedConnection);
            store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
        } catch (DAOException | SQLException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error de conexión al intentar actualizar el perfil.", e);
        }
    }

    public static String generatePassword() {
        return TEMPORARY_PASSWORD_PREFIX + UUID.randomUUID().toString().substring(0, TEMPORARY_PASSWORD_SUFFIX_LENGTH);
    }
}