package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import mx.uv.fei.TestApp;
import mx.uv.fei.TestConfig;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.ISchoolPeriodDAO;

@SpringBootTest(classes = TestApp.class)
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class SchoolPeriodDAOIT {

    @Autowired
    private ISchoolPeriodDAO periodDAOTest;

    private SchoolPeriod testPeriod;

    @BeforeEach
    void setUp() {
        testPeriod = new SchoolPeriod();
        
        testPeriod.setPeriodName("Febrero - Julio 2026");
        testPeriod.setStartDate(LocalDate.of(2026, 2, 10));
        testPeriod.setEndDate(LocalDate.of(2026, 7, 15));
        testPeriod.setStatus("proximo");
    }

    @Test
    void testInsertSchoolPeriodSuccess() {
        try {
            int resultId = periodDAOTest.insertSchoolPeriod(testPeriod);
            assertTrue(resultId > 0, "El periodo escolar debió registrarse exitosamente y devolver un ID mayor a 0");
        } catch (DAOException e) {
            fail("Falló la inserción del periodo escolar: " + e.getMessage());
        }
    }

    @Test
    void testRecoverSchoolPeriodSuccess() {
        try {
            int generatedId = periodDAOTest.insertSchoolPeriod(testPeriod);
            
            SchoolPeriod recovered = periodDAOTest.recoverSchoolPeriod(generatedId);

            assertNotNull(recovered, "El objeto recuperado no debería ser nulo");
            assertEquals("Febrero - Julio 2026", recovered.getPeriodName(), "El nombre del periodo debería coincidir");
            assertEquals(LocalDate.of(2026, 2, 10), recovered.getStartDate(), "La fecha de inicio debería coincidir");
            
        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testGetAllSchoolPeriodsSuccess() {
        try {
            periodDAOTest.insertSchoolPeriod(testPeriod);
            
            List<SchoolPeriod> list = periodDAOTest.getAllSchoolPeriods();
            
            assertTrue(list.size() > 0, "La lista debe contener al menos el periodo escolar que acabamos de insertar");
        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }

    @Test
    void testUpdateSchoolPeriodSuccess() {
        try {
            int generatedId = periodDAOTest.insertSchoolPeriod(testPeriod);

            testPeriod.setPeriodName("Agosto - Enero 2027");
            testPeriod.setStatus("activo");
            testPeriod.setStartDate(LocalDate.of(2026, 8, 15));

            boolean isUpdated = periodDAOTest.updateSchoolPeriod(testPeriod, generatedId);
            assertTrue(isUpdated, "La actualización debió devolver true");

            SchoolPeriod recovered = periodDAOTest.recoverSchoolPeriod(generatedId);
            assertEquals("Agosto - Enero 2027", recovered.getPeriodName(), "El nombre debió actualizarse");
            assertEquals("activo", recovered.getStatus(), "El estado debió actualizarse");
            assertEquals(LocalDate.of(2026, 8, 15), recovered.getStartDate(), "La fecha de inicio debió actualizarse");

        } catch (DAOException e) {
            fail("La prueba falló por una excepción: " + e.getMessage());
        }
    }
}