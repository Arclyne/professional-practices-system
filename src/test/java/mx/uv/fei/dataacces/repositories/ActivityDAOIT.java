package mx.uv.fei.dataacces.repositories;

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
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.domain.dto.Activity;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class ActivityDAOIT {

    @Autowired
    private IActivityDAO activityDAOTest;

    private Activity expectedActivity;

    @BeforeEach
    void setUp() {
        expectedActivity = new Activity();
        expectedActivity.setActivityId(1);
        expectedActivity.setName("Test");
        expectedActivity.setManager("Test");

    }

    @Test
    void testInsertActivity() {
        Activity testActivity = new Activity();

        testActivity.setName("Diseño de la Base de Datos");
        testActivity.setDescription("Creación del modelo Entidad-Relación y scripts SQL");
        testActivity.setManager("Josep Prueba");
        testActivity.setStartDate(java.sql.Date.valueOf("2026-04-15"));
        testActivity.setEndDate(java.sql.Date.valueOf("2026-04-30"));

        try {
            boolean resultTest = activityDAOTest.insertActivity(testActivity);
            assertTrue(resultTest);
        } catch (DAOException e) {

            String motivoReal = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            fail("La prueba falló por : " + motivoReal);
        }
    }

    @Test
    void testRecoverActivity() {

    }
}
