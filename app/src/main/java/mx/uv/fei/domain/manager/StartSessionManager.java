package mx.uv.fei.domain.manager;

import javafx.event.ActionEvent;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.AuthenticatorAction;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import java.util.Map;

@Component
public class StartSessionManager {

    private final IUserDAO userDAO;
    private final Store store;

    @Inject
    public StartSessionManager(IUserDAO userDAO, Store store) {
        this.userDAO = userDAO;
        this.store = store;
    }

    public void handleActionConnectButton(Map<String, String> credential, ActionEvent currentActionEvent)
            throws ManagerException {
        try {
            if (userDAO.verifyCredentials(credential.get("User"), credential.get("Password"))) {
                User retrievedUser = userDAO.getUserByUserName(credential.get("User"));

                store.dispatch(new AuthenticatorAction.LoginSuccess(retrievedUser));

                switch (retrievedUser.getStatus()) {
                    case "Pendiente":
                        store.dispatch(new NavigationAction.GoToSection(AppSection.PASSWORD_RESET));
                        break;

                    case "Activo":
                        store.dispatch(new NavigationAction.GoToSection(AppSection.TOKEN_VERIFICATION));
                        break;

                    case "No Activo":
                        throw new ManagerException("Usuario no activo. Comuníquese con su Coordinador.");

                    default:
                        throw new ManagerException("Estado de usuario no reconocido en el sistema.");
                }

            } else {
                throw new ManagerException("Usuario o Contraseña no valida.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema. Por favor, intente más tarde.", e);
        }
    }
}