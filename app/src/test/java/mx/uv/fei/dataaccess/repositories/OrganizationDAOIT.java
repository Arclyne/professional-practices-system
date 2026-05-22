package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
public class OrganizationDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IOrganizationDAO organizationTest;

    private Organization expectedOrganizationInserted;
    private Organization organizationToCompare01;
    private Organization organizationToCompare02;
    private List<Organization> expectedList;

    @BeforeEach
    void setUp() throws SQLException {

        assertNotNull(dbConnection, "dbConnection no fue inyectado.");
        assertNotNull(organizationTest, "organizationTest no fue inyectado.");

        TestDatabaseSetup.initialize(dbConnection);

        expectedOrganizationInserted = new Organization();
        expectedOrganizationInserted.setIdOrganization(1);
        expectedOrganizationInserted.setNameOrganization("toRecover");
        expectedOrganizationInserted.setMail("torecover@uv.mx");
        expectedOrganizationInserted.setState("Veracruz");
        expectedOrganizationInserted.setBusiness("Technology");

        organizationToCompare01 = new Organization();
        organizationToCompare01.setIdOrganization(2);
        organizationToCompare01.setNameOrganization("Dummy 1");
        organizationToCompare01.setMail("dummy1@uv.mx");
        organizationToCompare01.setState("Veracruz");
        organizationToCompare01.setBusiness("Technology");

        organizationToCompare02 = new Organization();
        organizationToCompare02.setIdOrganization(3);
        organizationToCompare02.setNameOrganization("Dummy 2");
        organizationToCompare02.setMail("dummy2@uv.mx");
        organizationToCompare02.setState("Veracruz");
        organizationToCompare02.setBusiness("Technology");

        expectedList = new ArrayList<>();
        expectedList.add(expectedOrganizationInserted);
        expectedList.add(organizationToCompare01);
        expectedList.add(organizationToCompare02);
    }

    @Test
    void testInsertOrganizationSuccess() throws DAOException {
        Organization testOrganization = new Organization();
        testOrganization.setNameOrganization("Python Software"); // Corregido typo
        testOrganization.setState("Veracruz");
        testOrganization.setAdress("Av. Xalapa");
        testOrganization.setCity("Xalapa");
        testOrganization.setBusiness("Derecho");
        testOrganization.setMail("python@softwareuv.mx");
        testOrganization.setCellphone("7485961234");

        boolean resultTest = organizationTest.insertOrganization(testOrganization);
        assertTrue(resultTest, "La organización debería haberse insertado exitosamente.");
    }

    @Test
    void testRecoverOrganizationSuccess() throws DAOException {
        Organization resultTest = organizationTest.recoverOrganization("toRecover");

        assertNotNull(resultTest, "No se pudo recuperar la organización.");
        assertEquals(expectedOrganizationInserted, resultTest,
                "La organización recuperada no coincide con la esperada.");
    }

    @Test
    void testRecoverALLSuccess() throws DAOException {
        List<Organization> resultTest = organizationTest.getAllOrganization();
        assertEquals(expectedList, resultTest,
                "La lista de organizaciones recuperada no coincide con la lista esperada.");
    }

    @Test
    void testUpdateTuplaSuccess() throws DAOException {
        Organization toUpdateOrganization = organizationTest.recoverOrganization("toRecover");
        assertNotNull(toUpdateOrganization, "La organización base para el update no existe.");

        Organization toUpdatedData = new Organization();
        toUpdatedData.setNameOrganization("UV Soft Updated");
        toUpdatedData.setState("Veracruz");
        toUpdatedData.setAdress("Nueva Direccion");
        toUpdatedData.setCity("Xalapa");
        toUpdatedData.setBusiness("Technology");
        toUpdatedData.setMail("nuevo_correo@uv.mx");
        toUpdatedData.setCellphone("1234567890");

        organizationTest.updateOrganization(toUpdatedData, toUpdateOrganization.getIdOrganization());

        Organization result = organizationTest.recoverOrganization("UV Soft Updated");

        assertNotNull(result, "No se pudo recuperar la organización tras la actualización.");
        assertEquals(toUpdatedData, result, "Los datos actualizados no coinciden.");
    }
}
