package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.sql.SQLException;
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

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPeriodDAO periodDAO;

    private Period testPeriod;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        testPeriod = new Period();
        testPeriod.setPeriodName("Agosto-Enero 2027");
        testPeriod.setStartDate(Date.valueOf("2026-08-15"));
        testPeriod.setEndDate(Date.valueOf("2027-01-31"));
        testPeriod.setPeriodStatus("Upcoming");
    }

    @Test
    void insertPeriod_ValidPeriod_ReturnsGeneratedId() throws DAOException {

        int generatedId = periodDAO.insertPeriod(testPeriod);

        assertTrue(generatedId > 0);
    }

    @Test
    void getAllPeriods_WithExistingData_ReturnsList() throws DAOException {

        List<Period> resultList = periodDAO.getAllPeriods();

        assertFalse(resultList.isEmpty());
    }
}
