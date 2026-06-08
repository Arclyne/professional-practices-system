package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private Project validProject;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        validProject = new Project();
        validProject.setProjectName("Sistema Integral FEI");
        validProject.setDescription("Desarrollo de modulos core del sistema de practicas");
        validProject.setParticipantCapacity(5);
        validProject.setManagerId(1);
        validProject.setStatus("Active");
        validProject.setStartDate(java.sql.Date.valueOf("2026-05-01"));
        validProject.setEndDate(java.sql.Date.valueOf("2026-12-01"));
        validProject.setCompanyId(1);
    }

    @Test
    void insertProject_ValidProject_ReturnsTrue() throws DAOException {

        boolean isInserted = projectDAO.insertProject(validProject);

        assertTrue(isInserted);
    }

    @Test
    void recoverProject_ExistingProject_ReturnsProject() throws DAOException {
        projectDAO.insertProject(validProject);

        Project recovered = projectDAO.recoverProject("Sistema Integral FEI", 1);

        assertEquals(validProject, recovered);
    }

    @Test
    void getAllProjects_WithExistingData_ReturnsList() throws DAOException {

        List<Project> resultList = projectDAO.getAllProjects();

        assertFalse(resultList.isEmpty());
    }

    @Test
    void updateProject_ValidModifiedData_ReturnsTrue() throws DAOException {
        projectDAO.insertProject(validProject);
        Project projectToUpdate = projectDAO.recoverProject("Sistema Integral FEI", 1);
        projectToUpdate.setDescription("Descripcion actualizada");
        projectToUpdate.setParticipantCapacity(10);

        boolean isUpdated = projectDAO.updateProject(projectToUpdate, projectToUpdate.getProjectId());

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
}