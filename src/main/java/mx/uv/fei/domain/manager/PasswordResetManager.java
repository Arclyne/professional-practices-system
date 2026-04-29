package mx.uv.fei.domain.manager;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;

import java.sql.Connection;
import java.sql.SQLException;

public class PasswordResetManager {

    private final IUserDAO userDAO;
    private final User userInSession;
    private final IDatabaseConnection databaseConnection;

    public PasswordResetManager(IDatabaseConnection databaseConnection, User userInSession) {
        this.userDAO = new UserDAO(databaseConnection);
        this.userInSession = userInSession;
        this.databaseConnection = databaseConnection;
    }

    public void updatePasswordAndActivate(String newPassword, String confirmPassword) throws ManagerException {
        if (newPassword == null || newPassword.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty()) {
            throw new ManagerException("Los campos de contraseña no pueden estar vacíos.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new ManagerException("Las contraseñas no coinciden. Por favor, verifique.");
        }
        userInSession.setPassword(newPassword);
        userInSession.setStatus("Activo");

        try (Connection sharedConnection = databaseConnection.getConnection()) {
            boolean isUpdated = userDAO.updateUser(userInSession, sharedConnection);

            if (!isUpdated) {
                throw new ManagerException("No se pudo actualizar la información en el sistema.");
            }
            Store.getInstance().dispatch(new NavigationAction.GoToSection(AppSection.LOGIN));
        } catch (DAOException | SQLException exception) {
            throw new ManagerException("Ocurrió un error de conexión al intentar actualizar el perfil.", exception);
        }
    }
}