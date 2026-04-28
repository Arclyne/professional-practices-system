package mx.uv.fei.domain.manager;

import java.util.regex.Pattern;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.repositories.AdministratorDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.AuthAction;

public class RegisterAdminManager {

    private final AdministratorDAO adminDAO;

    public RegisterAdminManager(IDatabaseConnection dbConnection) {
        UserDAO userDAO = new UserDAO(dbConnection);
        this.adminDAO = new AdministratorDAO(dbConnection, userDAO);
    }

    public boolean checkSystemHasAdmin() throws ManagerException {
        try {
            return this.adminDAO.checkIfAdminExists();
        } catch (DAOException e) {
            e.printStackTrace();
            throw new ManagerException("Error crítico al verificar el estado inicial del sistema.", e);
        }
    }

    public void registerInitialAdmin(Administrator administrator) throws ManagerException {
        administrator.setStatus("Activo");
        administrator.setRole("Administrador");

        this.validateAdminBusinessRules(administrator);

        try {
            int resultId = this.adminDAO.insertAdministrator(administrator);

            if (resultId <= 0) {
                throw new ManagerException("No se pudo completar el registro del administrador en el sistema.");
            }

            Store.getInstance().dispatch(new AuthAction.AdminCreatedSuccessfully());

        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión con el servidor o los datos ya existen. Por favor, verifique la información.", e);
        }
    }

    private void validateAdminBusinessRules(Administrator admin) throws ManagerException {
        if (admin.getUserName() == null || admin.getUserName().trim().isEmpty()) {
            throw new ManagerException("El nombre de usuario/identificador es obligatorio.");
        }

        if (admin.getPassword() == null || admin.getPassword().length() < 8) {
            throw new ManagerException("Por normativas de seguridad, la contraseña debe tener al menos 8 caracteres.");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.compile(emailRegex).matcher(admin.getEmail()).matches()) {
            throw new ManagerException("El formato del correo electrónico proporcionado no es válido.");
        }
        if (!admin.getUserName().matches("\\d+")) {
            throw new ManagerException("El identificador debe contener únicamente dígitos numéricos.");
        }
    }
}