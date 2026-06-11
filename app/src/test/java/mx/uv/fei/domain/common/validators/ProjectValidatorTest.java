package mx.uv.fei.domain.common.validators;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.Date;

import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

public class ProjectValidatorTest {

    @Test
    void validateProject_ValidProject_DoesNotThrowException() {
        Project validProject = new Project();
        validProject.setProjectName("Sistema de Inventario Web");
        validProject.setCompanyId(1);
        validProject.setManagerId(1);

        assertDoesNotThrow(() -> ProjectValidator.validateProjectData(validProject));
    }

    @Test
    void validateProject_EmptyName_ThrowsManagerException() {
        Project namelessProject = new Project();
        namelessProject.setProjectName("");

        assertThrows(ManagerException.class, () -> ProjectValidator.validateProjectData(namelessProject));
    }

    @Test
    void validateProject_InvalidDateRange_ThrowsManagerException() {
        Project invalidDatesProject = new Project();
        invalidDatesProject.setProjectName("Sistema de Inventario Web");
        invalidDatesProject.setCompanyId(1);
        invalidDatesProject.setManagerId(1);
        invalidDatesProject.setStartDate(Date.valueOf("2026-06-01"));
        invalidDatesProject.setEndDate(Date.valueOf("2026-05-01"));

        assertThrows(ManagerException.class, () -> ProjectValidator.validateProjectData(invalidDatesProject));
    }
}
