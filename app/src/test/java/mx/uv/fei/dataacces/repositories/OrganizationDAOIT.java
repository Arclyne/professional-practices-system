package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;

public class OrganizationDAOIT {

    private IDatabaseConnection dbConnection;
    private DatabasePropeties propeties;
    private IOrganizationDAO organizationTest;

    private Organization expectedOrganizationInserted;
    private Organization organizationToCompare01;
    private Organization organizationToCompare02;
    List<Organization> expectedList;

    @BeforeEach
    void setUp() throws SQLException {
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        TestDatabaseSetup.initialize(dbConnection);
        organizationTest = new OrganizationDAO(dbConnection);

        expectedOrganizationInserted = new Organization();
        expectedOrganizationInserted.setIdOrganization(1);
        expectedOrganizationInserted.setNameOrganization("toRecover");
        expectedOrganizationInserted.setMail("torecover@uv.mx");
        expectedOrganizationInserted.setRegion("Veracruz");
        expectedOrganizationInserted.setBusiness("Technology");

        organizationToCompare01 = new Organization();
        organizationToCompare01.setIdOrganization(2);
        organizationToCompare01.setNameOrganization("Dummy 1");
        organizationToCompare01.setMail("dummy1@uv.mx");
        organizationToCompare01.setRegion("Veracruz");
        organizationToCompare01.setBusiness("Technology");

        organizationToCompare02 = new Organization();
        organizationToCompare02.setIdOrganization(3);
        organizationToCompare02.setNameOrganization("Dummy 2");
        organizationToCompare02.setMail("dummy2@uv.mx");
        organizationToCompare02.setRegion("Veracruz");
        organizationToCompare02.setBusiness("Technology");

        expectedList = new ArrayList<Organization>();
        expectedList.add(expectedOrganizationInserted);
        expectedList.add(organizationToCompare01);
        expectedList.add(organizationToCompare02);
    }

    @Test
    void testInsertOrganizationSuccess() throws DAOException {
        Organization testOrganization = new Organization();
        testOrganization.setNameOrganization("Phython Software");
        testOrganization.setRegion("Veracruz");
        testOrganization.setAdress("Av. Xalapa");
        testOrganization.setCity("Xalapa");
        testOrganization.setBusiness("Derecho");
        testOrganization.setMail("python@softwareuv.mx");
        testOrganization.setCellphone("7485961234");

        boolean resultTest = organizationTest.insertOrganization(testOrganization);
        assertTrue(resultTest);

    }

    @Test
    void testRecoverOrganizationSuccess() throws DAOException {

        Organization resultTest = organizationTest.recoverOrganization("toRecover");
        assertEquals(expectedOrganizationInserted, resultTest);

    }

    @Test
    void testRecoverALLSuccess() throws DAOException {

        List<Organization> resultTest = organizationTest.getAllOrganization();
        assertEquals(expectedList, resultTest);

    }

    @Test
    void testUpdateTuplaSuccess() throws DAOException {

        Organization toUpdateOrganization = organizationTest.recoverOrganization("toRecover");

        Organization toUpdatedData = new Organization();
        toUpdatedData.setNameOrganization("UV Soft Updated");
        toUpdatedData.setRegion("Veracruz");
        toUpdatedData.setAdress("Nueva Direccion");
        toUpdatedData.setCity("Xalapa");
        toUpdatedData.setBusiness("Technology");
        toUpdatedData.setMail("nuevo_correo@uv.mx");
        toUpdatedData.setCellphone("1234567890");

        organizationTest.updateOrganization(toUpdatedData, toUpdateOrganization.getIdOrganization());

        toUpdateOrganization = organizationTest.recoverOrganization("UV Soft Updated");

        assertEquals(toUpdatedData, toUpdateOrganization);

    }
}
