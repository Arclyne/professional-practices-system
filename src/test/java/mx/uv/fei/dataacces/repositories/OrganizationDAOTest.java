package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Organization;

public class OrganizationDAOTest {

    OrganizationDAO orgDao;
    Organization org;

    @BeforeAll
    static void setup() {
        Organization org = new Organization();
        org.setAdress("Av.Xalapa");
        org.setBusiness("Finansas");
        org.setCellphone("22-88-22-99-65");
        org.setCity("Xalapa");
        org.setMail("correo@correo.com");
        org.setNameOrganization("PyhtonScryps");
        org.setRegion("Veracruz");

    }

    @Test
    void testInsertOrganization() {
        try {

            boolean result = orgDao.insertOrganization(org);
            assertTrue(result);

        } catch (DAOException e) {

            String motivoReal = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            fail("La prueba falló por : " + motivoReal);

        }
    }

    @Test
    void testRecoverOrganization() {

        Organization expectedResult = org;
        try {
            Organization test = orgDao.recoverOrganization("PyhtonScryps");
            assertEquals(expectedResult, test);
        } catch (DAOException e) {
            String motivoReal = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            fail("La prueba falló por: " + motivoReal);
        }
    }
}
