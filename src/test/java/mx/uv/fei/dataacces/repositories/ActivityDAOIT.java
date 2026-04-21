package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IActivityDAO;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;

public class ActivityDAOIT {

    private IDatabaseConnection dbConnection;
    private DatabasePropeties propeties;
    private IActivityDAO activityDAOTest;

    private Activity expectedActivityInserted;
    private Activity activityToCompare01;
    private Activity activityToCompare02;
    List<Activity> expectedList;

    @BeforeEach
    void setUp() {
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        activityDAOTest = new ActivityDAO(dbConnection);
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
    void testInsertActivitySuccess() throws DAOException {
        Activity testActivity = new Activity();

        testActivity.setName("Diseño de la Base de Datos");
        testActivity.setDescription("Creación del modelo Entidad-Relación y scripts SQL");
        testActivity.setManager("Josep Prueba");
        testActivity.setStartDate(java.sql.Date.valueOf("2026-04-15"));
        testActivity.setEndDate(java.sql.Date.valueOf("2026-04-30"));

        boolean resultTest = activityDAOTest.insertActivity(testActivity);
        assertTrue(resultTest);

    }

    @Test
    void testRecoverActivitySuccess() throws DAOException {

        Activity resultTest = activityDAOTest.recoverActivity("toRecover", "toRecover");
        assertEquals(expectedActivityInserted, resultTest);

    }

    @Test
    void testRecoverALSuccessL() throws DAOException {

        List<Activity> resultTest = activityDAOTest.getAllActivity();
        assertEquals(expectedList, resultTest);

    }

    @Test
    void testUpdateTuplaSuccess() throws DAOException {

        Activity toUpdateActivity = activityDAOTest.recoverActivity("toRecover", "toRecover");
        activityDAOTest.updateActivity(activityToCompare01, toUpdateActivity.getActivityId());
        toUpdateActivity = activityDAOTest.recoverActivity(activityToCompare01.getName(),
                activityToCompare01.getManager());

        assertEquals(activityToCompare01, toUpdateActivity);

    }
}