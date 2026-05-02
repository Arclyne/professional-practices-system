package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.SessionFacade; // <-- Tu nuevo Facade
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class PasswordManager {

    private final IUserDAO userDAO;
    private final IDatabaseConnection databaseConnection;
    private final SessionFacade session;
    private final Store store;

    @Inject
    public PasswordManager(IUserDAO userDAO, IDatabaseConnection databaseConnection, SessionFacade session,
            Store store) {
        this.userDAO = userDAO;
        this.databaseConnection = databaseConnection;
        this.session = session;
        this.store = store;
    }

    public void updatePasswordAndActivate(String newPassword, String confirmPassword) throws ManagerException {

        User userInSession = session.getCurrentUser();

        if (userInSession == null) {
            throw new ManagerException("Error: No se encontró una sesión activa para actualizar.");
        }

        userInSession.setStatus("Activo");
        userInSession.setPassword(newPassword);

        try (Connection sharedConnection = databaseConnection.getConnection()) {
            boolean isUpdated = userDAO.updateUser(userInSession, sharedConnection);

            if (!isUpdated) {
                throw new ManagerException("No se pudo actualizar la información en el sistema.");
            }

            store.dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));

        } catch (DAOException | SQLException exception) {
            throw new ManagerException("Ocurrió un error de conexión al intentar actualizar el perfil.", exception);
        }
    }
}