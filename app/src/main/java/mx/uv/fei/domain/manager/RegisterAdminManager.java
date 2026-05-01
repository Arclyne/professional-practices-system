package mx.uv.fei.domain.manager;

import java.util.regex.Pattern;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IAdministratorDAO;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.AuthenticatorAction;

@Component
public class RegisterAdminManager {

    private final IAdministratorDAO adminDAO;
    private final Store store;

    @Inject
    public RegisterAdminManager(IAdministratorDAO adminDAO, Store store) {
        this.adminDAO = adminDAO;
        this.store = store;
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
            store.dispatch(new AuthenticatorAction.AdminCreatedSuccessfully());

        } catch (DAOException e) {
            throw new ManagerException(
                    "Ocurrió un problema de conexión con el servidor o los datos ya existen. Por favor, verifique la información.",
                    e);
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