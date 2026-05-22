package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;

@StartEtiquetteTest
@Profile("test")
public class ProjectDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IProjectDAO projectDAOTest;

    private Project expectedProjectInserted;
    private Project projectToCompare01;
    private Project projectToCompare02;
    private List<Project> expectedList;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection);
        assertNotNull(projectDAOTest);

        TestDatabaseSetup.initialize(dbConnection);

        expectedProjectInserted = new Project();
        expectedProjectInserted.setProjectId(1);
        expectedProjectInserted.setProjectName("toRecover");
        expectedProjectInserted.setManager("toRecover");
        expectedProjectInserted.setCompanyId(1);

        projectToCompare01 = new Project();
        projectToCompare01.setProjectId(2);
        projectToCompare01.setProjectName("Dummy 1");
        projectToCompare01.setManager("Dummy 1");
        projectToCompare01.setCompanyId(1);

        projectToCompare02 = new Project();
        projectToCompare02.setProjectId(3);
        projectToCompare02.setProjectName("Dummy 2");
        projectToCompare02.setManager("Dummy 2");
        projectToCompare02.setCompanyId(1);

        expectedList = new ArrayList<>();
        expectedList.add(expectedProjectInserted);
        expectedList.add(projectToCompare01);
        expectedList.add(projectToCompare02);
    }

    @Test
    void testInsertProjectSuccess() throws DAOException {
        Project testProject = new Project();
        testProject.setProjectName("New Project Test");
        testProject.setDescription("Testing insertion logic");
        testProject.setParticipantCapacity(5);
        testProject.setManager("Test Manager");
        testProject.setStatus("Active");
        testProject.setStartDate(java.sql.Date.valueOf("2026-05-01"));
        testProject.setEndDate(java.sql.Date.valueOf("2026-12-01"));
        testProject.setCompanyId(1);

        boolean resultTest = projectDAOTest.insertProject(testProject);
        assertTrue(resultTest);
    }

    @Test
    void testRecoverProjectSuccess() throws DAOException {
        Project resultTest = projectDAOTest.recoverProject("toRecover", "toRecover");
        assertEquals(expectedProjectInserted, resultTest);
    }

    @Test
    void testRecoverALLSuccess() throws DAOException {
        List<Project> resultTest = projectDAOTest.getAllProjects();
        assertEquals(expectedList, resultTest);
    }

    @Test
    void testUpdateTuplaSuccess() throws DAOException {
        Project toUpdateProject = projectDAOTest.recoverProject("toRecover", "toRecover");

        Project toUpdatedData = new Project();
        toUpdatedData.setProjectName("Updated Project Name");
        toUpdatedData.setManager("Updated Manager");
        toUpdatedData.setDescription("Updated Description");
        toUpdatedData.setParticipantCapacity(10);
        toUpdatedData.setStatus("Inactive");
        toUpdatedData.setStartDate(java.sql.Date.valueOf("2026-06-01"));
        toUpdatedData.setEndDate(java.sql.Date.valueOf("2026-11-01"));
        toUpdatedData.setCompanyId(1);

        projectDAOTest.updateProject(toUpdatedData, toUpdateProject.getProjectId());

        Project resultTest = projectDAOTest.recoverProject("Updated Project Name", "Updated Manager");

        assertEquals(toUpdatedData, resultTest);
    }
}