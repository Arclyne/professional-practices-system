package mx.uv.fei.domain.manager;

import javafx.event.ActionEvent;
import mx.uv.fei.dataacces.interfaces.IAuthenticationToken;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.dataacces.repositories.AuthenticationTokenDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import java.util.Map;

public class StartSessionManager {

    private final IUserDAO userDAO;
    private final IAuthenticationToken token;

    public StartSessionManager(IDatabaseConnection databaseConnection) {
        this.userDAO = new UserDAO(databaseConnection);
        this.token = new AuthenticationTokenDAO(databaseConnection);
    }

    public void handleActionConnectButton(Map<String, String> credential, ActionEvent currentActionEvent) {

    }
}
