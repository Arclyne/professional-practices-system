package mx.uv.fei.domain.manager;

import javafx.event.ActionEvent;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.util.Map;

public class StartSessionManager {

    private final IUserDAO userDAO;
    private final User userInSession;

    public StartSessionManager(IDatabaseConnection databaseConnection, User userInSession) {
        this.userDAO = new UserDAO(databaseConnection);
        this.userInSession = userInSession;
    }

    public void handleActionConnectButton(Map<String, String> credential, ActionEvent currentActionEvent)
            throws ManagerException {
        try {
            if (userDAO.verifyCredentials(credential.get("User"), credential.get("Password"))) {
                User retrievedUser = userDAO.getUserByUserName(credential.get("User"));

                userInSession.setId(retrievedUser.getId());
                userInSession.setUserName(retrievedUser.getUserName());
                userInSession.setPassword(retrievedUser.getPassword());
                userInSession.setName(retrievedUser.getName());
                userInSession.setLastName(retrievedUser.getLastName());
                userInSession.setEmail(retrievedUser.getEmail());
                userInSession.setRole(retrievedUser.getRole());
                userInSession.setStatus(retrievedUser.getStatus());
                userInSession.setGender(retrievedUser.getGender());
                userInSession.setRegistrationDate(retrievedUser.getRegistrationDate());
                userInSession.setDischargeDate(retrievedUser.getDischargeDate());

                CommonControler.showInfoAlert("Registro Exitoso", "La actividad se ha guardado correctamente.");

                switch (userInSession.getStatus()) {
                    case "Pending": {

                        break;
                    }
                    case "Activo": {
                        break;
                    }
                    case "Inactivo": {
                        throw new ManagerException("Usuario Inactivo.Comuniquese con su Coordinador");
                    }
                    default:
                        break;
                }
            } else {
                throw new ManagerException("Usuario o Contraseña no valida");
            }
        } catch (

        DAOException e) {
            throw new ManagerException("Ocurrió un problema. Por favor, intente más tarde.", e);
        }
    }
}
