package mx.uv.fei.domain.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.SessionFacade;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;


@Component
public class PasswordManager {

    private static final String NO_SESSION_ERROR_MESSAGE = "Error: No se encontró una sesión activa para actualizar.";
    private static final String UPDATE_ERROR_MESSAGE = "No se pudo actualizar la información en el sistema.";
    private static final String CONNECTION_ERROR_MESSAGE = "Ocurrió un error de conexión al intentar actualizar el perfil.";
    private static final String TEMP_PREFIX = "temp-";
    private static final int SUBSTRING_START_INDEX = 0;
    private static final int SUBSTRING_END_INDEX = 8;

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

    public static String generateTemporaryPassword() {
        return TEMP_PREFIX + UUID.randomUUID().toString().substring(SUBSTRING_START_INDEX, SUBSTRING_END_INDEX);
    }

    public void updatePasswordAndActivate(String newPassword, String confirmPassword) throws ManagerException {
        User userInSession = session.getCurrentUser();

        if (userInSession == null) {
            throw new ManagerException(NO_SESSION_ERROR_MESSAGE);
        }

        userInSession.setStatus(UserStatus.ACTIVE);
        userInSession.setPassword(newPassword);

        try (Connection sharedConnection = databaseConnection.getConnection()) {
            boolean isUpdated = userDAO.updateUser(userInSession, sharedConnection);

            if (!isUpdated) {
                throw new ManagerException(UPDATE_ERROR_MESSAGE);
            }

            store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));

        } catch (DAOException | SQLException exception) {
            throw new ManagerException(CONNECTION_ERROR_MESSAGE, exception);
        }
    }
}