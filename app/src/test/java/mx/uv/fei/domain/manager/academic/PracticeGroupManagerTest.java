package mx.uv.fei.domain.manager.academic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        newGroup.setSection("12345");
        newGroup.setProfessorId(STORED_PROFESSOR_ID);
        newGroup.setPeriodId(STORED_PERIOD_ID);

        assertDoesNotThrow(() -> practiceGroupManager.registerNewPracticeGroup(newGroup));
    }

    @Test
    void registerNewPracticeGroup_DuplicateSectionInPeriod_ThrowsFriendlyMessageWithoutTechnicism() {
        PracticeGroup duplicateGroup = new PracticeGroup();
        duplicateGroup.setSection("60123");
        duplicateGroup.setProfessorId(STORED_PROFESSOR_ID);
        duplicateGroup.setPeriodId(STORED_PERIOD_ID);

        ManagerException thrownException = assertThrows(ManagerException.class,
                () -> practiceGroupManager.registerNewPracticeGroup(duplicateGroup));

        assertFriendlyDuplicateGroup(thrownException);
    }

    @Test
    void updatePracticeGroup_DuplicateSectionInPeriod_ThrowsFriendlyMessageWithoutTechnicism() throws ManagerException {
        PracticeGroup newGroup = new PracticeGroup();
        newGroup.setSection("12345");
        newGroup.setProfessorId(STORED_PROFESSOR_ID);
        newGroup.setPeriodId(STORED_PERIOD_ID);
        practiceGroupManager.registerNewPracticeGroup(newGroup);

        PracticeGroup groupToUpdate = findGroupBySection("12345");
        groupToUpdate.setSection("60123");

        ManagerException thrownException = assertThrows(ManagerException.class,
                () -> practiceGroupManager.updatePracticeGroup(groupToUpdate, groupToUpdate.getGroupId()));

        assertFriendlyDuplicateGroup(thrownException);
    }

    private PracticeGroup findGroupBySection(String section) throws ManagerException {
        return practiceGroupManager.getAllPracticeGroups().stream()
                .filter(group -> section.equals(group.getSection()))
                .findFirst()
                .orElseThrow();
    }

    private void assertFriendlyDuplicateGroup(ManagerException thrownException) {
        String message = thrownException.getMessage().toLowerCase();
        assertTrue(message.contains("grupo") || message.contains("secci") || message.contains("nrc"),
                "El mensaje debe indicar que ya existe el grupo en ese periodo: " + thrownException.getMessage());
        assertFalse(message.contains("duplicate") || message.contains("constraint")
                        || message.contains("sql") || message.contains("exception") || message.contains(".java"),
                "El mensaje no debe filtrar tecnicismos: " + thrownException.getMessage());
    }

    @Test
    void getAllPracticeGroups_ReturnsExpectedList() throws ManagerException {
        List<PracticeGroup> expectedGroups = new ArrayList<>();
        PracticeGroup storedGroup = new PracticeGroup();
        storedGroup.setGroupId(STORED_GROUP_ID);
        storedGroup.setSection("60123");
        storedGroup.setPeriodId(STORED_PERIOD_ID);
        storedGroup.setProfessorId(STORED_PROFESSOR_ID);
        expectedGroups.add(storedGroup);

        List<PracticeGroup> resultGroups = practiceGroupManager.getAllPracticeGroups();

        assertEquals(expectedGroups, resultGroups);
    }
}
