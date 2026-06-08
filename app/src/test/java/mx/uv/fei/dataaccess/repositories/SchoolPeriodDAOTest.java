package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.LocalDate;
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
    void getAllSchoolPeriods_WithExistingData_ReturnsList() throws DAOException {
        List<SchoolPeriod> resultList = periodDAO.getAllSchoolPeriods();
        assertFalse(resultList.isEmpty());
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
        SchoolPeriod expectedEmpty = new SchoolPeriod();
        SchoolPeriod recovered = periodDAO.recoverSchoolPeriod(9999);
        assertEquals(expectedEmpty, recovered);
    }

    @Test
    void updateSchoolPeriod_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = periodDAO.updateSchoolPeriod(testPeriod, 9999);
        assertFalse(isUpdated);
    }
}