package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.exceptions.DAOException;

public class PracticeGroupDAOTest {
    private PracticeGroupDAO groupDAO;
    private PracticeGroup testGroup;

    @BeforeEach
    void setUp() {
        groupDAO = new PracticeGroupDAO();
        testGroup = new PracticeGroup();
        testGroup.setSection("NRC-84932");
    }

    @Test
    void testInsertPracticeGroupSuccess() {
        try {
            ProfessorDAO professorDAO = new ProfessorDAO();
            Professor tempProf = new Professor();
            tempProf.setName("Profesor");
            tempProf.setLastName("Prueba Grupo");
            tempProf.setPassword("1234");
            tempProf.setStatus("activo");
            tempProf.setGender("Masculino");
            int validProfessorId = professorDAO.insertProfessor(tempProf);

            SchoolPeriodDAO periodDAO = new SchoolPeriodDAO();
            SchoolPeriod tempPeriod = new SchoolPeriod();
            tempPeriod.setPeriodName("Periodo Prueba Grupo");
            tempPeriod.setStartDate(LocalDate.of(2026, 8, 15));
            tempPeriod.setEndDate(LocalDate.of(2027, 1, 20));
            tempPeriod.setStatus("activo");
            int validPeriodId = periodDAO.insertSchoolPeriod(tempPeriod);

            assertTrue(validProfessorId > 0 && validPeriodId > 0, "No se pudieron crear las dependencias previas.");

            testGroup.setProfessorId(validProfessorId);
            testGroup.setPeriodId(validPeriodId);

            int resultId = groupDAO.insertPracticeGroup(testGroup);
            
            assertTrue(resultId > 0, "El grupo de prácticas debió registrarse exitosamente y devolver un ID mayor a 0");
            
        } catch (DAOException e) {
            fail("Falló la prueba del grupo de prácticas: " + e.getMessage());
        }
    }
}