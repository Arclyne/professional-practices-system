package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Organization;
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

    @Test
    void registerOrganization_ValidData_DoesNotThrow() {
        Organization org = new Organization();
        org.setNameOrganization("Nueva Org");
        org.setState("Veracruz");
        org.setMail("nueva@uv.mx");

        assertDoesNotThrow(() -> organizationManager.registerOrganization(org));
    }

    @Test
    void getAllOrganizations_ReturnsList() {
        assertDoesNotThrow(() -> organizationManager.getAllOrganizations());
    }

    @Test
    void inactivateMultipleOrganizations_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> organizationManager.inactivateMultipleOrganizations(List.of(1)));
    }
}