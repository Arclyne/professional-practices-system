package mx.uv.fei.domain.manager.identity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class AdminManagerTest {

    // Correo del administrador sembrado por data.sql; sigue existiendo tras inactivarlo.
    private static final String SEEDED_ADMIN_EMAIL = "rmarquez@uv.mx";

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private AdminManager adminManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
        inactivateSeededAdmins();
    }

    /**
     * data.sql siembra un administrador activo. La regla es "un solo administrador ACTIVO", así que
     * lo inactivamos para reproducir el escenario válido: inactivar al administrador en turno y dar
     * de alta a otro.
     */
    private void inactivateSeededAdmins() throws SQLException {
        try (Connection connection = dbConnection.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE user SET status = 'Inactive' WHERE role_name = 'Administrator'");
        }
    }

    @Test
    void registerInitialAdmin_NoActiveAdmin_DoesNotThrow() {
        Administrator newAdministrator = buildAdministrator("30014455", "jcastaneda@uv.mx");

        assertDoesNotThrow(() -> adminManager.registerInitialAdmin(newAdministrator));
    }

    @Test
    void registerInitialAdmin_DuplicateEmail_ThrowsDuplicateMessage() {
        // El correo ya existe en la tabla user: el INSERT debe rebotar contra la restricción UNIQUE y
        // traducirse a un mensaje claro de duplicidad, nunca al genérico de conexión.
        Administrator duplicateAdministrator = buildAdministrator("30099999", SEEDED_ADMIN_EMAIL);

        ManagerException exception = assertThrows(ManagerException.class,
                () -> adminManager.registerInitialAdmin(duplicateAdministrator));

        String message = exception.getMessage().toLowerCase();
        assertTrue(message.contains("registrado") || message.contains("uso"),
                "El mensaje debe indicar que el dato ya está en uso, no un error genérico. Fue: "
                        + exception.getMessage());
    }

    @Test
    void registerInitialAdmin_WhenActiveAdminExists_ThrowsActiveAdminMessage() {
        assertDoesNotThrow(() -> adminManager.registerInitialAdmin(buildAdministrator("30014455", "jcastaneda@uv.mx")));

        ManagerException exception = assertThrows(ManagerException.class,
                () -> adminManager.registerInitialAdmin(buildAdministrator("30015566", "otro.admin@uv.mx")));

        assertTrue(exception.getMessage().toLowerCase().contains("activo"),
                "Debe rechazar un segundo administrador activo. Fue: " + exception.getMessage());
    }

    private Administrator buildAdministrator(String username, String email) {
        Administrator administrator = new Administrator();
        administrator.setUserName(username);
        administrator.setPassword("AdminUv2026");
        administrator.setName("Jorge");
        administrator.setLastName("Castaneda Morales");
        administrator.setEmail(email);
        administrator.setGender(Gender.MALE);

        return administrator;
    }
}
