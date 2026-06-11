package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

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
}
