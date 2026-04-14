package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.exceptions.DAOException;

@SpringBootTest
@ActiveProfiles("test")
public class SchoolPeriodDAOTest {

    @Autowired
    private SchoolPeriodDAO periodDAO;
    @Autowired
    private SchoolPeriod testPeriod;

    @BeforeEach
    void setUp() {

        testPeriod.setPeriodName("Febrero - Julio 2026");
        testPeriod.setStartDate(LocalDate.of(2026, 2, 10));
        testPeriod.setEndDate(LocalDate.of(2026, 7, 15));
        testPeriod.setStatus("proximo");
    }

    @Test
    void testInsertSchoolPeriodSuccess() {
        try {
            int resultId = periodDAO.insertSchoolPeriod(testPeriod);

            assertTrue(resultId > 0, "El periodo escolar debió registrarse exitosamente y devolver un ID mayor a 0");

        } catch (DAOException e) {
            fail("Falló la inserción del periodo escolar: " + e.getMessage());
        }
    }
}