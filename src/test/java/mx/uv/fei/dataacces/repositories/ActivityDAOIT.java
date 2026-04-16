package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private Activity expectedActivityInserted;
    private Activity activityToCompare01;
    private Activity activityToCompare02;
    List<Activity> expectedList;

    @BeforeEach
    void setUp() {
        expectedActivityInserted = new Activity();
        expectedActivityInserted.setActivityId(1);
        expectedActivityInserted.setName("toRecover");
        expectedActivityInserted.setManager("toRecover");

        activityToCompare01 = new Activity();
        activityToCompare01.setActivityId(2);
        activityToCompare01.setName("Dummy 1");
        activityToCompare01.setManager("Dummy 1");

        activityToCompare02 = new Activity();
        activityToCompare02.setActivityId(3);
        activityToCompare02.setName("Dummy 2");
        activityToCompare02.setManager("Dummy 2");

        expectedList = new ArrayList<Activity>();
        expectedList.add(expectedActivityInserted);
        expectedList.add(activityToCompare01);
        expectedList.add(activityToCompare02);

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
        try {
            Activity resultTest = activityDAOTest.recoverActivity("toRecover", "toRecover");
            assertEquals(expectedActivityInserted, resultTest);
        } catch (DAOException e) {

            String motivoReal = (e.getCause() != null) ? e.getCause().getMessage() : e.getMessage();
            fail("La prueba falló por : " + motivoReal);
        }
    }

    @Test
    void testRecoverALL() {
        try {
            List<Activity> resultTest = activityDAOTest.getAllActivity();
            assertEquals(expectedList, resultTest);
        } catch (DAOException e) {
            fail("La prueba falló: " + e.getMessage());
        }
    }

    @Test
    void testUpdateTupla() {
        try {
            Activity toUpdateActivity = activityDAOTest.recoverActivity("toRecover", "toRecover");
            activityDAOTest.updateActivity(activityToCompare01, toUpdateActivity.getActivityId());
            toUpdateActivity = activityDAOTest.recoverActivity(activityToCompare01.getName(),
                    activityToCompare01.getManager());

            assertEquals(activityToCompare01, toUpdateActivity);
        } catch (DAOException e) {
            fail("La prueba falló: " + e.getMessage());
        }
    }
}