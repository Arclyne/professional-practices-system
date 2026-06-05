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
public class SchoolPeriodDAOTest {

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ISchoolPeriodDAO periodDAO;

    private SchoolPeriod testPeriod;

    @BeforeEach
    void setUp() throws SQLException {
        assertNotNull(dbConnection);
        assertNotNull(periodDAO);
        TestDatabaseSetup.initialize(dbConnection);
        testPeriod = new SchoolPeriod();
        testPeriod.setPeriodName("Febrero - Julio 2026");
        testPeriod.setStartDate(LocalDate.of(2026, 2, 10));
        testPeriod.setEndDate(LocalDate.of(2026, 7, 15));
        testPeriod.setStatus("proximo");
    }

    @Test
    void insertSchoolPeriod_ValidPeriod_ReturnsGeneratedId() throws DAOException {

        int resultId = periodDAO.insertSchoolPeriod(testPeriod);

        assertTrue(resultId > 0);
    }

    @Test
    void recoverSchoolPeriod_ExistingId_ReturnsPeriod() throws DAOException {
        int generatedId = periodDAO.insertSchoolPeriod(testPeriod);

        SchoolPeriod recovered = periodDAO.recoverSchoolPeriod(generatedId);

        assertEquals(testPeriod, recovered);
    }

    @Test
    void getAllSchoolPeriods_WithExistingData_ReturnsList() throws DAOException {
        periodDAO.insertSchoolPeriod(testPeriod);

        List<SchoolPeriod> list = periodDAO.getAllSchoolPeriods();

        assertFalse(list.isEmpty());
    }

    @Test
    void updateSchoolPeriod_ValidModifiedData_ReturnsUpdatedPeriod() throws DAOException {
        int generatedId = periodDAO.insertSchoolPeriod(testPeriod);
        testPeriod.setPeriodName("Agosto - Enero 2027");
        testPeriod.setStatus("activo");
        testPeriod.setStartDate(LocalDate.of(2026, 8, 15));

        periodDAO.updateSchoolPeriod(testPeriod, generatedId);
        SchoolPeriod recovered = periodDAO.recoverSchoolPeriod(generatedId);

        assertEquals(testPeriod, recovered);
    }
}