package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
import mx.uv.fei.domain.dto.Period;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PeriodDAOTest {

    private static final int STORED_PERIOD_ID = 5;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPeriodDAO periodDAO;

    private Period newPeriod;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newPeriod = new Period();
        newPeriod.setPeriodName("Agosto 2026 - Enero 2027");
        newPeriod.setStartDate(Date.valueOf("2026-08-15"));
        newPeriod.setEndDate(Date.valueOf("2027-01-31"));
        newPeriod.setPeriodStatus("Upcoming");
    }

    @Test
    void insertPeriod_ValidPeriod_ReturnsGeneratedId() throws DAOException {
        int generatedId = periodDAO.insertPeriod(newPeriod);

        assertTrue(generatedId > 0);
    }

    @Test
    void getAllPeriods_WithExistingData_ReturnsExpectedList() throws DAOException {
        List<Period> expectedPeriods = new ArrayList<>();
        Period storedPeriod = new Period();
        storedPeriod.setPeriodId(STORED_PERIOD_ID);
        storedPeriod.setPeriodName("Junio-Diciembre 2026");
        storedPeriod.setStartDate(Date.valueOf("2026-06-01"));
        storedPeriod.setEndDate(Date.valueOf("2026-12-12"));
        storedPeriod.setPeriodStatus("Active");
        expectedPeriods.add(storedPeriod);

        List<Period> resultPeriods = periodDAO.getAllPeriods();

        assertEquals(expectedPeriods, resultPeriods);
    }
}
