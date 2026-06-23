package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
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

    private static final String STORED_PROJECT_NAME = "Sistema de Inventario Web";
    private static final int FIRST_PROJECT_ID = 1;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IProjectDAO projectDAO;

    private Project newProject;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newProject = new Project();
        newProject.setProjectName("Sistema Integral FEI");
        newProject.setDescription("Desarrollo de modulos core del sistema de practicas");
        newProject.setParticipantCapacity(5);
        newProject.setManagerId(1);
        newProject.setStatus("Active");
        newProject.setStartDate(Date.valueOf("2026-05-01"));
        newProject.setEndDate(Date.valueOf("2026-12-01"));
        newProject.setCompanyId(1);
    }

    private List<Project> buildStoredProjects() {
        List<Project> storedProjects = new ArrayList<>();

        Project inventoryProject = new Project();
        inventoryProject.setProjectId(1);
        inventoryProject.setProjectName(STORED_PROJECT_NAME);
        inventoryProject.setDescription("Desarrollo de un sistema web para el control de inventario");
        inventoryProject.setParticipantCapacity(2);
        inventoryProject.setManagerId(1);
        inventoryProject.setStatus("Active");
        inventoryProject.setStartDate(Date.valueOf("2026-01-01"));
        inventoryProject.setEndDate(Date.valueOf("2026-06-01"));
        inventoryProject.setCompanyId(1);
        storedProjects.add(inventoryProject);

        Project salesProject = new Project();
        salesProject.setProjectId(2);
        salesProject.setProjectName("Aplicacion Movil de Ventas");
        salesProject.setDescription("Desarrollo de una aplicacion movil para la gestion de ventas");
        salesProject.setParticipantCapacity(3);
        salesProject.setManagerId(2);
        salesProject.setStatus("Active");
        salesProject.setStartDate(Date.valueOf("2026-01-01"));
        salesProject.setEndDate(Date.valueOf("2026-06-01"));
        salesProject.setCompanyId(2);
        storedProjects.add(salesProject);

        Project humanResourcesProject = new Project();
        humanResourcesProject.setProjectId(3);
        humanResourcesProject.setProjectName("Portal de Recursos Humanos");
        humanResourcesProject.setDescription("Mantenimiento del portal interno de recursos humanos");
        humanResourcesProject.setParticipantCapacity(1);
        humanResourcesProject.setManagerId(3);
        humanResourcesProject.setStatus("Active");
        humanResourcesProject.setStartDate(Date.valueOf("2026-01-01"));
        humanResourcesProject.setEndDate(Date.valueOf("2026-06-01"));
        humanResourcesProject.setCompanyId(3);
        storedProjects.add(humanResourcesProject);

        return storedProjects;
    }

    @Test
    void insertProject_ValidProject_ReturnsTrue() throws DAOException {
        int isInserted = projectDAO.insertProject(newProject);

        assertTrue(isInserted > 0);
    }

    @Test
    void recoverProject_ExistingProject_ReturnsProject() throws DAOException {
        Project expectedProject = buildStoredProjects().get(0);

        Project recoveredProject = projectDAO.recoverProject(STORED_PROJECT_NAME, FIRST_PROJECT_ID);

        assertEquals(expectedProject, recoveredProject);
    }

    @Test
    void getAllProjects_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Project> expectedProjects = buildStoredProjects();

        List<Project> resultProjects = projectDAO.getAllProjects();

        assertEquals(expectedProjects, resultProjects);
    }

    @Test
    void updateProject_ValidModifiedData_ReturnsTrue() throws DAOException {
        newProject.setDescription("Mantenimiento de los modulos de reportes del sistema");
        newProject.setParticipantCapacity(10);

        assertDoesNotThrow(() -> projectDAO.updateProject(newProject, FIRST_PROJECT_ID));
    }

    @Test
    void deactivateMultipleProjects_ValidIds_ReturnsTrue() throws DAOException {
        assertDoesNotThrow(() -> projectDAO.deactivateMultipleProjects(List.of(1, 2)));
    }

    @Test
    void getAvailableProjectsWithCapacity_WithActiveProjects_ReturnsExpectedList() throws DAOException {
        List<Project> expectedProjects = buildStoredProjects();

        List<Project> resultProjects = projectDAO.getAvailableProjectsWithCapacity();

        assertEquals(expectedProjects, resultProjects);
    }

    @Test
    void insertProject_NonExistentOrganization_ThrowsDAOException() {
        newProject.setCompanyId(NON_EXISTENT_ID);

        assertThrows(DAOException.class, () -> projectDAO.insertProject(newProject));
    }

    @Test
    void recoverProject_NonExistentName_ReturnsEmptyProject() throws DAOException {
        Project recoveredProject = projectDAO.recoverProject("Proyecto Inexistente", FIRST_PROJECT_ID);

        assertEquals(new Project(), recoveredProject);
    }

    @Test
    void updateProject_NonExistentId_ReturnsFalse() throws DAOException {
        assertDoesNotThrow(() -> projectDAO.updateProject(newProject, NON_EXISTENT_ID));
    }
}
