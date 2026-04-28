package mx.uv.fei.domain.manager;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IAdministratorDAO;
import mx.uv.fei.dataacces.repositories.AdministratorDAO;
import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.exceptions.ManagerException;

import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.AuthAction;

import java.util.regex.Pattern;

public class RegisterAdminManager {

    private final IAdministratorDAO adminDAO;

    // Patrón de inyección de dependencias simple
    public RegisterAdminManager(IAdministratorDAO adminDAO) {
        this.adminDAO = adminDAO;
    }

    // Constructor por defecto para facilitar instanciación rápida en el Controlador
    public RegisterAdminManager() {
        // Inicializamos los DAOs necesarios
        DatabaseConnection connection = new DatabaseConnection();
        UserDAO userDAO = new UserDAO(connection);
        this.adminDAO = new AdministratorDAO(connection, userDAO);
    }

    /**
     * Método utilizado por la lógica de arranque (Splash Screen) para decidir qué vista cargar.
     */
    public boolean checkSystemHasAdmin() throws ManagerException {
        try {
            return adminDAO.checkIfAdminExists();
        } catch (DAOException e) {
            throw new ManagerException("Error crítico al verificar el estado inicial del sistema.", e);
        }
    }

    /**
     * Registra al primer administrador, aplicando reglas de negocio y actualizando el estado global.
     */
    public void registerInitialAdmin(Administrator admin) throws ManagerException {
        // 1. Validaciones de Reglas de Negocio (El dominio es el experto aquí)
        validateAdminBusinessRules(admin);

        try {
            // Aquí podrías agregar lógica para encriptar la contraseña antes de mandarla al DAO
            // admin.setPassword(HashUtil.hash(admin.getPassword()));

            // 2. Interacción con la capa de persistencia
            int generatedId = adminDAO.insertAdministrator(admin);

            if (generatedId <= 0) {
                throw new ManagerException("No se pudo completar el registro del administrador en el sistema.");
            }

            // 3. ¡El Manager avisa al Estado Global!
            // Al hacer esto aquí, desacoplamos la UI de la lógica de negocio.
            Store.getInstance().dispatch(new AuthAction.AdminCreatedSuccessfully());

        } catch (DAOException e) {
            // Traducción de excepciones técnicas a excepciones de negocio
            String errorMessage = "Error en la base de datos al registrar administrador.";

            // Ejemplo de manejo de duplicados (dependiendo de cómo arroje el error tu driver SQL)
            if (e.getMessage().toLowerCase().contains("duplicate") || e.getMessage().toLowerCase().contains("unique")) {
                errorMessage = "El correo electrónico o número de personal ya está en uso.";
            }

            throw new ManagerException(errorMessage, e);
        }
    }

    /**
     * Helper privado para centralizar las reglas de negocio de la facultad.
     */
    private void validateAdminBusinessRules(Administrator admin) throws ManagerException {
        if (admin.getPassword().length() < 8) {
            throw new ManagerException("Por normativas de seguridad, la contraseña debe tener al menos 8 caracteres.");
        }

        // Expresión regular simple para validar formato de correo
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!Pattern.compile(emailRegex).matcher(admin.getEmail()).matches()) {
            throw new ManagerException("El formato del correo electrónico proporcionado no es válido.");
        }

        // Validación de Número de Personal (ej. que solo contenga números si esa es la regla)
        if (!admin.getStaffNumber().matches("\\d+")) {
            throw new ManagerException("El número de personal debe contener únicamente dígitos numéricos.");
        }

        // El estado y fecha de registro no dependen del usuario, el sistema los define
        admin.setStatus("activo");
    }
}