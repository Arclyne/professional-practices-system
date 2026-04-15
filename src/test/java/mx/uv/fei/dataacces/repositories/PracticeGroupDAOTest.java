package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.exceptions.DAOException;

@SpringBootTest
@ActiveProfiles("test")
public class PracticeGroupDAOTest {
    @Autowired
    private PracticeGroupDAO groupDAO;
    @Autowired
    private PracticeGroup testGroup;
    @Autowired
    private ProfessorDAO professorDAO;
    @Autowired
    private SchoolPeriodDAO periodDAO;

    @BeforeEach
    void setUp() {
        testGroup.setSection("NRC-84932");
    }

    @Test
    void testInsertPracticeGroupSuccess() {
        try {

            Professor tempProf = new Professor();
            tempProf.setName("Profesor");
            tempProf.setLastName("Prueba Grupo");
            tempProf.setPassword("1234");
            tempProf.setStatus("activo");
            tempProf.setGender("Masculino");
            int validProfessorId = professorDAO.insertProfessor(tempProf);

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