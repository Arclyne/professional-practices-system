package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;

@StartEtiquetteTest
@Profile("test")
public class OrganizationDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IOrganizationDAO organizationDAO;

    private Organization testOrganization;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testOrganization = new Organization();
        testOrganization.setNameOrganization("Python Software");
        testOrganization.setState("Veracruz");
        testOrganization.setAdress("Av. Xalapa");
        testOrganization.setCity("Xalapa");
        testOrganization.setBusiness("Technology");
        testOrganization.setMail("python@softwareuv.mx");
        testOrganization.setCellphone("7485961234");
    }

    @Test
    void insertOrganization_ValidOrganization_ReturnsTrue() throws DAOException {
        boolean resultTest = organizationDAO.insertOrganization(testOrganization);
        assertTrue(resultTest);
    }

    @Test
    void recoverOrganization_ExistingName_ReturnsOrganization() throws DAOException {
        Organization expectedOrganization = new Organization();
        expectedOrganization.setIdOrganization(1);
        expectedOrganization.setNameOrganization("toRecover");
        expectedOrganization.setState("Active");
        expectedOrganization.setBusiness("Technology");
        expectedOrganization.setMail("torecover@uv.mx");

        Organization resultTest = organizationDAO.recoverOrganization("toRecover");
        assertEquals(expectedOrganization, resultTest);
    }

    @Test
    void getAllOrganizations_WithExistingData_ReturnsExpectedList() throws DAOException {
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

        List<Organization> resultTest = organizationDAO.getAllOrganizations();
        assertEquals(expectedList, resultTest);
    }

    @Test
    void updateOrganization_ValidModifiedData_ReturnsTrue() throws DAOException {
        testOrganization.setNameOrganization("UV Soft Updated");
        boolean isUpdated = organizationDAO.updateOrganization(testOrganization, 1);
        assertTrue(isUpdated);
    }

    @Test
    void deactivateMultipleOrganizations_ValidIds_ReturnsTrue() throws DAOException {
        boolean isDeactivated = organizationDAO.deactivateMultipleOrganizations(List.of(1, 2));
        assertTrue(isDeactivated);
    }

    @Test
    void insertOrganization_DuplicateEmail_ThrowsDAOException() {
        testOrganization.setMail("torecover@uv.mx");
        assertThrows(DAOException.class, () -> organizationDAO.insertOrganization(testOrganization));
    }

    @Test
    void recoverOrganization_NonExistentName_ReturnsEmptyOrganization() throws DAOException {
        Organization resultTest = organizationDAO.recoverOrganization("Organizacion Fantasma");
        assertEquals(new Organization(), resultTest);
    }

    @Test
    void updateOrganization_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = organizationDAO.updateOrganization(testOrganization, 9999);
        assertFalse(isUpdated);
    }
}