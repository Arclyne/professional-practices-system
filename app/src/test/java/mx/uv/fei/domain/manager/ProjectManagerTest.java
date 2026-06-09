package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ProjectManagerTest {

    @Inject private IDatabaseConnection dbConnection;
    @Inject private ProjectManager projectManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewProject_ValidData_DoesNotThrow() {
        Project p = new Project();
        p.setProjectName("New Project");
        p.setCompanyId(1);
        p.setManagerId(1);
        p.setStartDate(Date.valueOf("2026-01-01"));
        p.setEndDate(Date.valueOf("2026-06-01"));
        assertDoesNotThrow(() -> projectManager.registerNewProject(p));
    }

    @Test
    void getAllProjects_ReturnsExpectedList() throws ManagerException {
        List<Project> expectedList = new ArrayList<>();

        Project p1 = new Project();
        p1.setProjectId(1);
        p1.setProjectName("toRecover");
        p1.setDescription("Project for recovery test");
        p1.setParticipantCapacity(2);
        p1.setManagerId(1);
        p1.setStatus("Active");
        p1.setStartDate(java.sql.Date.valueOf("2026-01-01"));
        p1.setEndDate(java.sql.Date.valueOf("2026-06-01"));
        p1.setCompanyId(1);
        expectedList.add(p1);

        Project p2 = new Project();
        p2.setProjectId(2);
        p2.setProjectName("Dummy 1");
        p2.setDescription("First dummy project");
        p2.setParticipantCapacity(3);
        p2.setManagerId(2);
        p2.setStatus("Active");
        p2.setStartDate(java.sql.Date.valueOf("2026-01-01"));
        p2.setEndDate(java.sql.Date.valueOf("2026-06-01"));
        p2.setCompanyId(2);
        expectedList.add(p2);

        Project p3 = new Project();
        p3.setProjectId(3);
        p3.setProjectName("Dummy 2");
        p3.setDescription("Second dummy project");
        p3.setParticipantCapacity(1);
        p3.setManagerId(3);
        p3.setStatus("Active");
        p3.setStartDate(java.sql.Date.valueOf("2026-01-01"));
        p3.setEndDate(java.sql.Date.valueOf("2026-06-01"));
        p3.setCompanyId(3);
        expectedList.add(p3);

        List<Project> resultList = projectManager.getAllProjects();
        assertEquals(expectedList, resultList);
    }

    @Test
    void inactivateMultipleProjects_ValidList_DoesNotThrow() {
        assertDoesNotThrow(() -> projectManager.inactivateMultipleProjects(java.util.List.of(1)));
    }
}