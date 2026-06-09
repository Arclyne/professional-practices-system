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

    @Inject private IDatabaseConnection dbConnection;
    @Inject private PracticeGroupManager practiceGroupManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewPracticeGroup_ValidGroup_DoesNotThrow() {
        PracticeGroup group = new PracticeGroup();
        group.setSection("NRC-12345");
        group.setProfessorId(68);
        group.setPeriodId(5);
        assertDoesNotThrow(() -> practiceGroupManager.registerNewPracticeGroup(group));
    }

    @Test
    void getAllPracticeGroups_ReturnsExpectedList() throws ManagerException {
        List<PracticeGroup> expectedList = new ArrayList<>();
        PracticeGroup group = new PracticeGroup();
        group.setGroupId(6);
        group.setSection("Seccion G");
        group.setPeriodId(5);
        group.setProfessorId(68);
        expectedList.add(group);

        List<PracticeGroup> resultList = practiceGroupManager.getAllPracticeGroups();
        assertEquals(expectedList, resultList);
    }
}