package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import mx.uv.fei.TestApp;
import mx.uv.fei.TestConfig;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IOrganizationDAO;
import mx.uv.fei.domain.dto.Organization;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class OrganizationDAOIT {

    @Autowired
    private IOrganizationDAO organizationTest;

    private Organization expectedOrganization;

    @BeforeEach
    void setUp() {
        expectedOrganization = new Organization();
        expectedOrganization.setIdOrganization(1);
        expectedOrganization.setNameOrganization("UV Soft");
        expectedOrganization.setMail("contacto@uv.mx");
        expectedOrganization.setRegion("Veracruz");
    }

    @Test
    void testInsertOrganization() {

        Organization test = new Organization();
        test.setNameOrganization("Phython Software");
        test.setRegion("Veracruz");
        test.setAdress("Av. Xalapa");
        test.setCity("Xalapa");
        test.setBusiness("Derecho");
        test.setMail("python@softwareuv.mx");
        test.setCellphone("7485961234");

        try {
            boolean result = organizationTest.insertOrganization(test);
            assertTrue(result);
        } catch (DAOException e) {

            String motivoReal = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            fail("La prueba falló por : " + motivoReal);

        }
    }

    @Test
    void testRecoverOrganization() {

        try {
            Organization result = organizationTest.recoverOrganization("UV Soft");

            assertNotNull(result);
            assertEquals(expectedOrganization, result);
        } catch (DAOException e) {
            String motivoReal = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            fail("La prueba falló por: " + motivoReal);
        }
    }
}
