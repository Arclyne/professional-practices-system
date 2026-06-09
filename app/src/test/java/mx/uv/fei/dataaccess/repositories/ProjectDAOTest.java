package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")

public class ProjectDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IProjectDAO projectDAO;

    private Project testProject;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testProject = new Project();
        testProject.setProjectName("Sistema Integral FEI");
        testProject.setDescription("Desarrollo de modulos core del sistema de practicas");
        testProject.setParticipantCapacity(5);
        testProject.setManagerId(1);
        testProject.setStatus("Active");
        testProject.setStartDate(java.sql.Date.valueOf("2026-05-01"));
        testProject.setEndDate(java.sql.Date.valueOf("2026-12-01"));
        testProject.setCompanyId(1);
    }

    @Test
    void insertProject_ValidProject_ReturnsTrue() throws DAOException {
        boolean isInserted = projectDAO.insertProject(testProject);
        assertTrue(isInserted);
    }

    @Test
    void recoverProject_ExistingProject_ReturnsProject() throws DAOException {
        Project expectedProject = new Project();
        expectedProject.setProjectId(1);
        expectedProject.setProjectName("toRecover");
        expectedProject.setDescription("Project for recovery test");
        expectedProject.setParticipantCapacity(2);
        expectedProject.setManagerId(1);
        expectedProject.setStatus("Active");
        expectedProject.setStartDate(java.sql.Date.valueOf("2026-01-01"));
        expectedProject.setEndDate(java.sql.Date.valueOf("2026-06-01"));
        expectedProject.setCompanyId(1);

        Project recovered = projectDAO.recoverProject("toRecover", 1);
        assertEquals(expectedProject, recovered);
    }

    @Test
    void getAllProjects_WithExistingData_ReturnsList() throws DAOException {
        List<Project> resultList = projectDAO.getAllProjects();
        assertFalse(resultList.isEmpty());
    }

    @Test
    void updateProject_ValidModifiedData_ReturnsTrue() throws DAOException {
        testProject.setDescription("Descripcion actualizada");
        testProject.setParticipantCapacity(10);

        boolean isUpdated = projectDAO.updateProject(testProject, 1);
        assertTrue(isUpdated);
    }

    @Test
    void deactivateMultipleProjects_ValidIds_ReturnsTrue() throws DAOException {
        boolean isDeactivated = projectDAO.deactivateMultipleProjects(List.of(1, 2));
        assertTrue(isDeactivated);
    }

    @Test
    void getAvailableProjectsWithCapacity_WithActiveProjects_ReturnsList() throws DAOException {
        List<Project> resultList = projectDAO.getAvailableProjectsWithCapacity();
        assertFalse(resultList.isEmpty());
    }

    @Test
    void insertProject_NonExistentOrganization_ThrowsDAOException() {
        testProject.setCompanyId(9999);
        assertThrows(DAOException.class, () -> {
            projectDAO.insertProject(testProject);
        });
    }

    @Test
    void recoverProject_NonExistentName_ReturnsEmptyProject() throws DAOException {
        Project expectedEmpty = new Project();
        Project recovered = projectDAO.recoverProject("Proyecto Fantasma", 1);
        assertEquals(expectedEmpty, recovered);
    }

    @Test
    void updateProject_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = projectDAO.updateProject(testProject, 9999);
        assertFalse(isUpdated);
    }
}
