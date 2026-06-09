package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

    @Test
    void registerOrganization_ValidData_DoesNotThrow() {
        Organization org = new Organization();
        org.setNameOrganization("Nueva Org");
        org.setState("Veracruz");
        org.setMail("nueva@uv.mx");

        assertDoesNotThrow(() -> organizationManager.registerOrganization(org));
    }

    @Test
    void getAllOrganizations_ReturnsExpectedList() throws ManagerException {
        List<Organization> expectedList = new ArrayList<>();

        Organization org1 = new Organization();
        org1.setIdOrganization(1);
        org1.setNameOrganization("toRecover");
        org1.setState("Active");
        org1.setBusiness("Technology");
        org1.setMail("torecover@uv.mx");
        expectedList.add(org1);

        Organization org2 = new Organization();
        org2.setIdOrganization(2);
        org2.setNameOrganization("Dummy 1");
        org2.setState("Active");
        org2.setBusiness("Technology");
        org2.setMail("dummy1@uv.mx");
        expectedList.add(org2);

        Organization org3 = new Organization();
        org3.setIdOrganization(3);
        org3.setNameOrganization("Dummy 2");
        org3.setState("Active");
        org3.setBusiness("Technology");
        org3.setMail("dummy2@uv.mx");
        expectedList.add(org3);

        List<Organization> resultList = organizationManager.getAllOrganizations();
        assertEquals(expectedList, resultList);
    }

    @Test
    void inactivateMultipleOrganizations_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> organizationManager.inactivateMultipleOrganizations(List.of(1)));
    }
}