package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ActivityManagerTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ActivityManager activityManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerActivity_ValidActivity_DoesNotThrow() {
        Activity activity = new Activity();
        activity.setPractitionerId(123);
        activity.setTitle("Test");
        activity.setDescription("Desc");
        activity.setActivityDate(Date.valueOf("2026-06-01"));
        activity.setDurationHours(2);

        assertDoesNotThrow(() -> activityManager.registerActivity(activity));
    }

    @Test
    void getPractitionerLogbook_ValidId_ReturnsList() throws ManagerException {
        List<Activity> list = activityManager.getPractitionerLogbook(123);
        assertFalse(list.isEmpty());
    }
}
