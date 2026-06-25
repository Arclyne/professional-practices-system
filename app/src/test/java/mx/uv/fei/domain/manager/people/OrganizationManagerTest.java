package mx.uv.fei.domain.manager.people;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class OrganizationManagerTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private OrganizationManager organizationManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    private static final int ORGANIZATION_NAME_LIMIT = 150;

    @Test
    void registerOrganization_ValidData_DoesNotThrow() {
        Organization newOrganization = new Organization();
        newOrganization.setNameOrganization("Soluciones Digitales del Golfo");
        newOrganization.setState("Veracruz");
        newOrganization.setMail("contacto@solucionesgolfo.mx");

        assertDoesNotThrow(() -> organizationManager.registerOrganization(newOrganization));
    }

    @Test
    void registerOrganization_NameExceedsLimit_ThrowsManagerException() {
        Organization oversizedNameOrganization = new Organization();
        oversizedNameOrganization.setNameOrganization("A".repeat(ORGANIZATION_NAME_LIMIT + 1));
        oversizedNameOrganization.setMail("contacto@solucionesgolfo.mx");

        assertThrows(ManagerException.class, () -> organizationManager.registerOrganization(oversizedNameOrganization));
    }

    @Test
    void registerOrganization_InvalidEmailFormat_ThrowsManagerException() {
        Organization invalidEmailOrganization = new Organization();
        invalidEmailOrganization.setNameOrganization("Soluciones Digitales del Golfo");
        invalidEmailOrganization.setMail("correo-invalido");

        assertThrows(ManagerException.class, () -> organizationManager.registerOrganization(invalidEmailOrganization));
    }

    @Test
    void getAllOrganizations_ReturnsExpectedList() throws ManagerException {
        List<Organization> expectedOrganizations = new ArrayList<>();

        Organization firstOrganization = new Organization();
        firstOrganization.setIdOrganization(1);
        firstOrganization.setNameOrganization("Tecnologias Web del Golfo");
        firstOrganization.setState("Active");
        firstOrganization.setBusiness("Technology");
        firstOrganization.setMail("contacto@tecgolfo.mx");
        expectedOrganizations.add(firstOrganization);

        Organization secondOrganization = new Organization();
        secondOrganization.setIdOrganization(2);
        secondOrganization.setNameOrganization("Consultoria Digital Xalapa");
        secondOrganization.setState("Active");
        secondOrganization.setBusiness("Technology");
        secondOrganization.setMail("contacto@cdxalapa.mx");
        expectedOrganizations.add(secondOrganization);

        Organization thirdOrganization = new Organization();
        thirdOrganization.setIdOrganization(3);
        thirdOrganization.setNameOrganization("Software Veracruzano");
        thirdOrganization.setState("Active");
        thirdOrganization.setBusiness("Technology");
        thirdOrganization.setMail("contacto@softver.mx");
        expectedOrganizations.add(thirdOrganization);

        List<Organization> resultOrganizations = organizationManager.getAllOrganizations();

        assertEquals(expectedOrganizations, resultOrganizations);
    }

    @Test
    void registerOrganization_DuplicateEmail_ThrowsFriendlyMessageWithoutTechnicism() {
        Organization duplicateEmailOrganization = new Organization();
        duplicateEmailOrganization.setNameOrganization("Otra Empresa del Golfo");
        duplicateEmailOrganization.setState("Veracruz");
        duplicateEmailOrganization.setMail("contacto@tecgolfo.mx");

        ManagerException thrownException = assertThrows(ManagerException.class,
                () -> organizationManager.registerOrganization(duplicateEmailOrganization));

        assertFriendlyDuplicateEmail(thrownException);
    }

    @Test
    void updateOrganization_DuplicateEmail_ThrowsFriendlyMessageWithoutTechnicism() {
        Organization organizationToUpdate = new Organization();
        organizationToUpdate.setNameOrganization("Consultoria Digital Xalapa");
        organizationToUpdate.setState("Active");
        organizationToUpdate.setBusiness("Technology");
        organizationToUpdate.setMail("contacto@tecgolfo.mx");

        ManagerException thrownException = assertThrows(ManagerException.class,
                () -> organizationManager.updateOrganization(organizationToUpdate, 2));

        assertFriendlyDuplicateEmail(thrownException);
    }

    private void assertFriendlyDuplicateEmail(ManagerException thrownException) {
        String message = thrownException.getMessage().toLowerCase();
        assertTrue(message.contains("correo"),
                "El mensaje debe indicar que el correo ya está en uso: " + thrownException.getMessage());
        assertFalse(message.contains("duplicate") || message.contains("constraint")
                        || message.contains("sql") || message.contains("exception") || message.contains(".java"),
                "El mensaje no debe filtrar tecnicismos de base de datos: " + thrownException.getMessage());
    }

    @Test
    void inactivateMultipleOrganizations_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> organizationManager.inactivateMultipleOrganizations(List.of(1)));
    }

    @Test
    void inactivateOrganization_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> organizationManager.inactivateOrganization(1));
    }

    @Test
    void activateOrganization_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> organizationManager.activateOrganization(1));
    }
}
