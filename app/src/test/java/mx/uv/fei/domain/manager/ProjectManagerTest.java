package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.Date;
import java.sql.SQLException;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Project;
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
}