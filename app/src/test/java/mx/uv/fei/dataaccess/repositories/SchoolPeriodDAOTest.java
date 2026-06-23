package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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

    private static final int STORED_PERIOD_ID = 5;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ISchoolPeriodDAO periodDAO;

    private SchoolPeriod newPeriod;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newPeriod = new SchoolPeriod();
        newPeriod.setPeriodName("Febrero-Julio 2026");
        newPeriod.setStartDate(LocalDate.of(2026, 2, 10));
        newPeriod.setEndDate(LocalDate.of(2026, 7, 15));
        newPeriod.setStatus("Upcoming");
    }

    private SchoolPeriod buildStoredPeriod() {
        SchoolPeriod storedPeriod = new SchoolPeriod();
        storedPeriod.setPeriodId(STORED_PERIOD_ID);
        storedPeriod.setPeriodName("Junio-Diciembre 2026");
        storedPeriod.setStartDate(LocalDate.of(2026, 6, 1));
        storedPeriod.setEndDate(LocalDate.of(2026, 12, 12));
        storedPeriod.setStatus("Active");
        return storedPeriod;
    }

    @Test
    void insertSchoolPeriod_ValidPeriod_ReturnsGeneratedId() throws DAOException {
        int resultId = periodDAO.insertSchoolPeriod(newPeriod);

        assertTrue(resultId > 0);
    }

    @Test
    void recoverSchoolPeriod_ExistingId_ReturnsPeriod() throws DAOException {
        SchoolPeriod expectedPeriod = buildStoredPeriod();

        SchoolPeriod recoveredPeriod = periodDAO.recoverSchoolPeriod(STORED_PERIOD_ID);

        assertEquals(expectedPeriod, recoveredPeriod);
    }

    @Test
    void getAllSchoolPeriods_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<SchoolPeriod> expectedPeriods = new ArrayList<>();
        expectedPeriods.add(buildStoredPeriod());

        List<SchoolPeriod> resultPeriods = periodDAO.getAllSchoolPeriods();

        assertEquals(expectedPeriods, resultPeriods);
    }

    @Test
    void updateSchoolPeriod_ValidModifiedData_ReturnsTrue() throws DAOException {
        newPeriod.setPeriodName("Agosto 2026 - Enero 2027");
        newPeriod.setStatus("Active");
        newPeriod.setStartDate(LocalDate.of(2026, 8, 15));

        assertDoesNotThrow(() -> periodDAO.updateSchoolPeriod(newPeriod, STORED_PERIOD_ID));
    }

    @Test
    void recoverSchoolPeriod_NonExistentId_ReturnsEmptyPeriod() throws DAOException {
        SchoolPeriod recoveredPeriod = periodDAO.recoverSchoolPeriod(NON_EXISTENT_ID);

        assertEquals(new SchoolPeriod(), recoveredPeriod);
    }

    @Test
    void updateSchoolPeriod_NonExistentId_ReturnsFalse() throws DAOException {
        assertDoesNotThrow(() -> periodDAO.updateSchoolPeriod(newPeriod, NON_EXISTENT_ID));
    }
}
