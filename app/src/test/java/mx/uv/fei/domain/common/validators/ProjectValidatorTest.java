package mx.uv.fei.domain.common.validators;

import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import java.sql.Date;

public class ProjectValidatorTest {

    @Test
    void validateProject_ValidProject_DoesNotThrowException() {
        Project project = new Project();
        project.setProjectName("Test Project");
        project.setCompanyId(1);
        project.setManagerId(1);
        assertDoesNotThrow(() -> ProjectValidator.validateProjectData(project));
    }

    @Test
    void validateProject_EmptyName_ThrowsManagerException() {
        Project project = new Project();
        project.setProjectName("");
        assertThrows(ManagerException.class, () -> ProjectValidator.validateProjectData(project));
    }

    @Test
    void validateProject_InvalidDateRange_ThrowsManagerException() {
        Project project = new Project();
        project.setProjectName("Test");
        project.setCompanyId(1);
        project.setManagerId(1);
        project.setStartDate(Date.valueOf("2026-06-01"));
        project.setEndDate(Date.valueOf("2026-05-01"));
        assertThrows(ManagerException.class, () -> ProjectValidator.validateProjectData(project));
    }
}
