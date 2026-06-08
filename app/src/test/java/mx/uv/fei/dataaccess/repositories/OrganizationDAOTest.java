package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        expectedOrganization.setMail("torecover@uv.mx");
        expectedOrganization.setState("Active");
        expectedOrganization.setBusiness("Technology");

        Organization resultTest = organizationDAO.recoverOrganization("toRecover");
        assertEquals(expectedOrganization, resultTest);
    }

    @Test
    void getAllOrganizations_WithExistingData_ReturnsList() throws DAOException {
        List<Organization> resultTest = organizationDAO.getAllOrganizations();
        assertFalse(resultTest.isEmpty());
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
        assertThrows(DAOException.class, () -> {
            organizationDAO.insertOrganization(testOrganization);
        });
    }

    @Test
    void recoverOrganization_NonExistentName_ReturnsEmptyOrganization() throws DAOException {
        Organization expectedEmpty = new Organization();
        Organization resultTest = organizationDAO.recoverOrganization("Organizacion Fantasma");
        assertEquals(expectedEmpty, resultTest);
    }

    @Test
    void updateOrganization_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = organizationDAO.updateOrganization(testOrganization, 9999);
        assertFalse(isUpdated);
    }
}