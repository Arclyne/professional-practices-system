package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Organization;

public class OrganizationDAOTest {
    @Test
    void testInsertOrganization() {
        OrganizationDAO orgDao = new OrganizationDAO();
        Organization org = new Organization();
        org.setAdress("Av.Xalapa");
        org.setBusiness("Finansas");
        org.setCellphone("22-88-22-99-65");
        org.setCity("Xalapa");
        org.setMail("correo@correo.com");
        org.setNameOrganization("PyhtonScryps");
        org.setRegion("Veracruz");

        try {
            boolean result = orgDao.insertOrganization(org);
            assertNotNull(result);
        } catch (DAOException e) {
            e.printStackTrace();

            // Extraemos el mensaje real de MySQL usando getCause()
            String motivoReal = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            fail("La prueba falló. MySQL dice: " + motivoReal);
        }
    }

    @Test
    void testRecoverOrganization() {
        OrganizationDAO organizationDAO = new OrganizationDAO();
        try {
            Organization test = organizationDAO.recoverOrganization("PyhtonScryps");
            assertNotNull(test);
        } catch (DAOException e) {
            e.printStackTrace();

            // Extraemos el mensaje real de MySQL usando getCause()
            String motivoReal = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            fail("La prueba falló. MySQL dice: " + motivoReal);
        }
    }
}
