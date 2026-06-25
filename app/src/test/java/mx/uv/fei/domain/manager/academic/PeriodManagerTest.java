package mx.uv.fei.domain.manager.academic;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.Date;
import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PeriodManagerTest {

    private static final int STORED_PERIOD_ID = 5;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private PeriodManager periodManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewPeriod_ValidPeriod_DoesNotThrow() {
        Period newPeriod = new Period();
        newPeriod.setPeriodName("Agosto 2027 - Enero 2028");
        newPeriod.setStartDate(Date.valueOf("2027-08-01"));
        newPeriod.setEndDate(Date.valueOf("2028-01-31"));

        assertDoesNotThrow(() -> periodManager.registerNewPeriod(newPeriod));
    }

    @Test
    void registerNewPeriod_InvalidPeriod_ThrowsManagerException() {
        Period emptyPeriod = new Period();

        assertThrows(ManagerException.class, () -> periodManager.registerNewPeriod(emptyPeriod));
    }

    @Test
    void registerNewPeriod_StartDateAfterEndDate_ThrowsManagerException() {
        Period invertedPeriod = new Period();
        invertedPeriod.setPeriodName("Periodo con fechas invertidas");
        invertedPeriod.setStartDate(Date.valueOf("2028-01-31"));
        invertedPeriod.setEndDate(Date.valueOf("2027-08-01"));

        assertThrows(ManagerException.class, () -> periodManager.registerNewPeriod(invertedPeriod));
    }

    @Test
    void registerNewPeriod_StartDateEqualsEndDate_ThrowsManagerException() {
        Period sameDayPeriod = new Period();
        sameDayPeriod.setPeriodName("Periodo de un solo dia");
        sameDayPeriod.setStartDate(Date.valueOf("2027-08-01"));
        sameDayPeriod.setEndDate(Date.valueOf("2027-08-01"));

        assertThrows(ManagerException.class, () -> periodManager.registerNewPeriod(sameDayPeriod));
    }

    @Test
    void getPeriodById_StoredId_ReturnsMatchingPeriod() throws ManagerException {
        Period storedPeriod = periodManager.getPeriodById(STORED_PERIOD_ID);

        assertEquals(STORED_PERIOD_ID, storedPeriod.getPeriodId());
    }

    @Test
    void activatePeriod_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> periodManager.activatePeriod(STORED_PERIOD_ID));
    }

    @Test
    void inactivatePeriod_StoredId_DoesNotThrow() {
        assertDoesNotThrow(() -> periodManager.inactivatePeriod(STORED_PERIOD_ID));
    }

    @Test
    void updatePeriod_ValidData_DoesNotThrow() throws ManagerException {
        Period periodToUpdate = periodManager.getPeriodById(STORED_PERIOD_ID);
        periodToUpdate.setPeriodName("Junio-Diciembre 2026 (editado)");

        assertDoesNotThrow(() -> periodManager.updatePeriod(periodToUpdate, STORED_PERIOD_ID));
    }

    @Test
    void registerNewPeriod_NewPeriod_IsRegisteredAsUpcoming() throws ManagerException {
        Period newPeriod = new Period();
        newPeriod.setPeriodName("Periodo Proximo 2030");
        newPeriod.setStartDate(Date.valueOf("2030-08-01"));
        newPeriod.setEndDate(Date.valueOf("2031-01-31"));

        periodManager.registerNewPeriod(newPeriod);

        assertEquals("Upcoming", findPeriodByName("Periodo Proximo 2030").getPeriodStatus());
    }

    @Test
    void activatePeriod_AnotherPeriodAlreadyActive_ThrowsManagerException() throws ManagerException {
        Period newPeriod = new Period();
        newPeriod.setPeriodName("Periodo Por Activar 2031");
        newPeriod.setStartDate(Date.valueOf("2031-08-01"));
        newPeriod.setEndDate(Date.valueOf("2032-01-31"));
        periodManager.registerNewPeriod(newPeriod);
        int newPeriodId = findPeriodByName("Periodo Por Activar 2031").getPeriodId();

        assertThrows(ManagerException.class, () -> periodManager.activatePeriod(newPeriodId));
    }

    private Period findPeriodByName(String periodName) throws ManagerException {
        return periodManager.getAllPeriods().stream()
                .filter(period -> periodName.equals(period.getPeriodName()))
                .findFirst()
                .orElseThrow();
    }
}
