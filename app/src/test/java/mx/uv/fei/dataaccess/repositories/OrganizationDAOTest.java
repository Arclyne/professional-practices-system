package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class OrganizationDAOTest {

    private static final String STORED_ORGANIZATION_NAME = "Tecnologias Web del Golfo";
    private static final int FIRST_ORGANIZATION_ID = 1;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IOrganizationDAO organizationDAO;

    private Organization newOrganization;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newOrganization = new Organization();
        newOrganization.setNameOrganization("Soluciones Digitales del Golfo");
        newOrganization.setState("Veracruz");
        newOrganization.setAdress("Av. Lazaro Cardenas 850");
        newOrganization.setCity("Xalapa");
        newOrganization.setBusiness("Technology");
        newOrganization.setMail("contacto@solucionesgolfo.mx");
        newOrganization.setCellphone("2288456790");
    }

    private Organization buildFirstStoredOrganization() {
        Organization storedOrganization = new Organization();
        storedOrganization.setIdOrganization(FIRST_ORGANIZATION_ID);
        storedOrganization.setNameOrganization(STORED_ORGANIZATION_NAME);
        storedOrganization.setState("Active");
        storedOrganization.setBusiness("Technology");
        storedOrganization.setMail("contacto@tecgolfo.mx");
        return storedOrganization;
    }

    @Test
    void insertOrganization_ValidOrganization_ReturnsTrue() throws DAOException {
        int isInserted = organizationDAO.insertOrganization(newOrganization);

        assertTrue(isInserted > 0);
    }

    @Test
    void recoverOrganization_ExistingName_ReturnsOrganization() throws DAOException {
        Organization expectedOrganization = buildFirstStoredOrganization();

        Organization recoveredOrganization = organizationDAO.recoverOrganization(STORED_ORGANIZATION_NAME);

        assertEquals(expectedOrganization, recoveredOrganization);
    }

    @Test
    void getAllOrganizations_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Organization> expectedOrganizations = new ArrayList<>();
        expectedOrganizations.add(buildFirstStoredOrganization());

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

        List<Organization> resultOrganizations = organizationDAO.getAllOrganizations();

        assertEquals(expectedOrganizations, resultOrganizations);
    }

    @Test
    void updateOrganization_ValidModifiedData_ReturnsTrue() throws DAOException {
        newOrganization.setNameOrganization("Soluciones Digitales de Veracruz");

        assertDoesNotThrow(() -> organizationDAO.updateOrganization(newOrganization, FIRST_ORGANIZATION_ID));
    }

    @Test
    void deactivateMultipleOrganizations_ValidIds_DoesNotThrow() {
        assertDoesNotThrow(() -> organizationDAO.deactivateMultipleOrganizations(List.of(1, 2)));
    }

    @Test
    void insertOrganization_DuplicateEmail_ThrowsDAOException() {
        newOrganization.setMail("contacto@tecgolfo.mx");

        assertThrows(DAOException.class, () -> organizationDAO.insertOrganization(newOrganization));
    }

    @Test
    void recoverOrganization_NonExistentName_ReturnsEmptyOrganization() throws DAOException {
        Organization recoveredOrganization = organizationDAO.recoverOrganization("Organizacion Inexistente");

        assertEquals(new Organization(), recoveredOrganization);
    }

    @Test
    void updateOrganization_NonExistentId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> organizationDAO.updateOrganization(newOrganization, NON_EXISTENT_ID));
    }
}
