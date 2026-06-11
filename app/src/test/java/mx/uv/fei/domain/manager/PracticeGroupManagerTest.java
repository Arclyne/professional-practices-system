package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PracticeGroupManagerTest {

    private static final int STORED_GROUP_ID = 6;
    private static final int STORED_PROFESSOR_ID = 68;
    private static final int STORED_PERIOD_ID = 5;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private PracticeGroupManager practiceGroupManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewPracticeGroup_ValidGroup_DoesNotThrow() {
        PracticeGroup newGroup = new PracticeGroup();
        newGroup.setSection("NRC-12345");
        newGroup.setProfessorId(STORED_PROFESSOR_ID);
        newGroup.setPeriodId(STORED_PERIOD_ID);

        assertDoesNotThrow(() -> practiceGroupManager.registerNewPracticeGroup(newGroup));
    }

    @Test
    void getAllPracticeGroups_ReturnsExpectedList() throws ManagerException {
        List<PracticeGroup> expectedGroups = new ArrayList<>();
        PracticeGroup storedGroup = new PracticeGroup();
        storedGroup.setGroupId(STORED_GROUP_ID);
        storedGroup.setSection("Seccion 601");
        storedGroup.setPeriodId(STORED_PERIOD_ID);
        storedGroup.setProfessorId(STORED_PROFESSOR_ID);
        expectedGroups.add(storedGroup);

        List<PracticeGroup> resultGroups = practiceGroupManager.getAllPracticeGroups();

        assertEquals(expectedGroups, resultGroups);
    }
}
