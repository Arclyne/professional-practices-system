package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProjectDAO;
import mx.uv.fei.domain.dto.Project;

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
        assertNotNull(dbConnection);
        assertNotNull(projectDAO);
        TestDatabaseSetup.initialize(dbConnection);

        validProject = new Project();
        validProject.setProjectName("Sistema Integral FEI");
        validProject.setDescription("Desarrollo de módulos core del sistema de prácticas");
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


        assertTrue(isInserted, "El proyecto debió registrarse exitosamente en la base de datos.");
    }

    @Test
    void updateProject_ValidModifiedData_ReturnsTrue() throws DAOException {

        projectDAO.insertProject(validProject);
        Project projectToUpdate = projectDAO.recoverProject("Sistema Integral FEI", 1);

        projectToUpdate.setDescription("Descripción Actualizada desde la prueba unitaria");
        projectToUpdate.setParticipantCapacity(10);


        boolean isUpdated = projectDAO.updateProject(projectToUpdate, projectToUpdate.getProjectId());


        assertTrue(isUpdated, "La actualización del proyecto debió ejecutarse y retornar true.");
    }
}