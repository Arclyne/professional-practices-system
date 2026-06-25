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

    // Usuario ya sembrado por data.sql (mismo username), usado para forzar la colisión de unicidad.
    private static final String EXISTING_USERNAME = "30011111";

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private AdminManager adminManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
        clearAdministrators();
    }

    /**
     * El sistema arranca sin administrador (data.sql siembra uno, así que lo retiramos) para poder
     * ejercitar el alta del administrador inicial.
     */
    private void clearAdministrators() throws SQLException {
        try (Connection connection = dbConnection.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM administrator");
        }
    }

    @Test
    void registerInitialAdmin_ValidAdmin_DoesNotThrow() {
        Administrator initialAdministrator = buildAdministrator("30014455", "jcastaneda@uv.mx");

        assertDoesNotThrow(() -> adminManager.registerInitialAdmin(initialAdministrator));
    }

    @Test
    void registerInitialAdmin_DuplicateUserData_ThrowsDuplicateMessage() {
        // El username ya existe en la tabla user (otro rol), aunque no haya administrador: el INSERT
        // debe rebotar contra la restricción UNIQUE y traducirse a un mensaje claro de duplicidad
        // ("ya está en uso" / "ya está registrado"), nunca al genérico error de conexión.
        Administrator duplicateAdministrator = buildAdministrator(EXISTING_USERNAME, "otro.correo@uv.mx");

        ManagerException exception = assertThrows(ManagerException.class,
                () -> adminManager.registerInitialAdmin(duplicateAdministrator));

        String message = exception.getMessage().toLowerCase();
        assertTrue(message.contains("uso") || message.contains("registrado"),
                "El mensaje debe indicar que el dato ya está en uso, no un error genérico de conexión. Fue: "
                        + exception.getMessage());
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
