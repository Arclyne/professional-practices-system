package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.ISchoolPeriodDAO;

@StartEtiquetteTest
@Profile("test")
public class SchoolPeriodDAOIT {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ISchoolPeriodDAO periodDAOTest;

    private SchoolPeriod testPeriod;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection);
        assertNotNull(periodDAOTest);

        TestDatabaseSetup.initialize(dbConnection);

        testPeriod = new SchoolPeriod();
        testPeriod.setPeriodName("Febrero - Julio 2026");
        testPeriod.setStartDate(LocalDate.of(2026, 2, 10));
        testPeriod.setEndDate(LocalDate.of(2026, 7, 15));
        testPeriod.setStatus("proximo");
    }

    @Test
    void testInsertSchoolPeriodSuccess() throws DAOException {
        int resultId = periodDAOTest.insertSchoolPeriod(testPeriod);
        assertTrue(resultId > 0);
    }

    @Test
    void testRecoverSchoolPeriodSuccess() throws DAOException {
        int generatedId = periodDAOTest.insertSchoolPeriod(testPeriod);
        SchoolPeriod recovered = periodDAOTest.recoverSchoolPeriod(generatedId);
        assertEquals(testPeriod, recovered);
    }

    @Test
    void testGetAllSchoolPeriodsSuccess() throws DAOException {
        periodDAOTest.insertSchoolPeriod(testPeriod);
        List<SchoolPeriod> list = periodDAOTest.getAllSchoolPeriods();
        assertFalse(list.isEmpty());
    }

    @Test
    void testUpdateSchoolPeriodSuccess() throws DAOException {
        int generatedId = periodDAOTest.insertSchoolPeriod(testPeriod);

        testPeriod.setPeriodName("Agosto - Enero 2027");
        testPeriod.setStatus("activo");
        testPeriod.setStartDate(LocalDate.of(2026, 8, 15));

        periodDAOTest.updateSchoolPeriod(testPeriod, generatedId);

        SchoolPeriod recovered = periodDAOTest.recoverSchoolPeriod(generatedId);
        assertEquals(testPeriod, recovered);
    }
}