package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.ISchoolPeriodDAO;
import mx.uv.fei.domain.dto.SchoolPeriod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class SchoolPeriodDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ISchoolPeriodDAO periodDAO;

    private SchoolPeriod testPeriod;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testPeriod = new SchoolPeriod();
        testPeriod.setPeriodName("Febrero-Julio 2026");
        testPeriod.setStartDate(LocalDate.of(2026, 2, 10));
        testPeriod.setEndDate(LocalDate.of(2026, 7, 15));
        testPeriod.setStatus("Upcoming");
    }

    @Test
    void insertSchoolPeriod_ValidPeriod_ReturnsGeneratedId() throws DAOException {
        int resultId = periodDAO.insertSchoolPeriod(testPeriod);
        assertTrue(resultId > 0);
    }

    @Test
    void recoverSchoolPeriod_ExistingId_ReturnsPeriod() throws DAOException {
        SchoolPeriod expectedPeriod = new SchoolPeriod();
        expectedPeriod.setPeriodId(5);
        expectedPeriod.setPeriodName("Junio-Diciembre 2026");
        expectedPeriod.setStartDate(LocalDate.of(2026, 6, 1));
        expectedPeriod.setEndDate(LocalDate.of(2026, 12, 12));
        expectedPeriod.setStatus("Active");

        SchoolPeriod recovered = periodDAO.recoverSchoolPeriod(5);
        assertEquals(expectedPeriod, recovered);
    }

    @Test
    void getAllSchoolPeriods_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<SchoolPeriod> expectedList = new ArrayList<>();
        SchoolPeriod sp = new SchoolPeriod();
        sp.setPeriodId(5);
        sp.setPeriodName("Junio-Diciembre 2026");
        sp.setStartDate(LocalDate.of(2026, 6, 1));
        sp.setEndDate(LocalDate.of(2026, 12, 12));
        sp.setStatus("Active");
        expectedList.add(sp);

        List<SchoolPeriod> resultList = periodDAO.getAllSchoolPeriods();
        assertEquals(expectedList, resultList);
    }

    @Test
    void updateSchoolPeriod_ValidModifiedData_ReturnsTrue() throws DAOException {
        testPeriod.setPeriodName("Agosto-Enero 2027");
        testPeriod.setStatus("Active");
        testPeriod.setStartDate(LocalDate.of(2026, 8, 15));

        boolean isUpdated = periodDAO.updateSchoolPeriod(testPeriod, 5);
        assertTrue(isUpdated);
    }

    @Test
    void recoverSchoolPeriod_NonExistentId_ReturnsEmptyPeriod() throws DAOException {
        SchoolPeriod recovered = periodDAO.recoverSchoolPeriod(9999);
        assertEquals(new SchoolPeriod(), recovered);
    }

    @Test
    void updateSchoolPeriod_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = periodDAO.updateSchoolPeriod(testPeriod, 9999);
        assertFalse(isUpdated);
    }
}