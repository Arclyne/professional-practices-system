package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.*;
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

    @Inject private IDatabaseConnection dbConnection;
    @Inject private PeriodManager periodManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerNewPeriod_ValidPeriod_DoesNotThrow() {
        Period period = new Period();
        period.setPeriodName("2027-2028");
        period.setStartDate(Date.valueOf("2027-08-01"));
        period.setEndDate(Date.valueOf("2028-01-31"));
        assertDoesNotThrow(() -> periodManager.registerNewPeriod(period));
    }

    @Test
    void registerNewPeriod_InvalidPeriod_ThrowsManagerException() {
        Period period = new Period();
        assertThrows(ManagerException.class, () -> periodManager.registerNewPeriod(period));
    }
}