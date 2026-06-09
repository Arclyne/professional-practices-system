package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Administrator;
import mx.uv.fei.domain.enums.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class AdminManagerTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private AdminManager adminManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerInitialAdmin_ValidAdmin_DoesNotThrow() {
        Administrator admin = new Administrator();
        admin.setUserName("11111");
        admin.setPassword("Password123");
        admin.setName("Admin");
        admin.setLastName("Test");
        admin.setEmail("admin2@uv.mx");
        admin.setGender(Gender.MALE);

        assertDoesNotThrow(() -> adminManager.registerInitialAdmin(admin));
    }
}
