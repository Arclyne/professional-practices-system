package mx.uv.fei.dataacces.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.DatabasePropeties;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.ISchoolPeriodDAO;

public class SchoolPeriodDAOIT {

    private IDatabaseConnection dbConnection;
    private DatabasePropeties propeties;

    private ISchoolPeriodDAO periodDAOTest;
    private SchoolPeriod testPeriod;

    @BeforeEach
    void setUp() throws SQLException {
        propeties = new DatabasePropeties();
        dbConnection = new DataconnectionConfig(propeties, "test").databaseConnection();
        TestDatabaseSetup.initialize(dbConnection);

        periodDAOTest = new SchoolPeriodDAO(dbConnection);
        testPeriod = new SchoolPeriod();

        testPeriod.setPeriodName("Febrero - Julio 2026");
        testPeriod.setStartDate(LocalDate.of(2026, 2, 10));
        testPeriod.setEndDate(LocalDate.of(2026, 7, 15));
        testPeriod.setStatus("proximo");
    }

    @Test
    void testInsertSchoolPeriodSuccess() throws DAOException {
        int resultId = periodDAOTest.insertSchoolPeriod(testPeriod);

        assertTrue(resultId > 0, "El periodo escolar debió registrarse exitosamente y devolver un ID mayor a 0");
    }

    @Test
    void testRecoverSchoolPeriodSuccess() throws DAOException {
        int generatedId = periodDAOTest.insertSchoolPeriod(testPeriod);

        SchoolPeriod recovered = periodDAOTest.recoverSchoolPeriod(generatedId);

        assertEquals(testPeriod, recovered, "El periodo escolar recuperado no coincide con el insertado.");
    }

    @Test
    void testGetAllSchoolPeriodsSuccess() throws DAOException {
        periodDAOTest.insertSchoolPeriod(testPeriod);

        List<SchoolPeriod> list = periodDAOTest.getAllSchoolPeriods();

        assertFalse(list.isEmpty(), "La lista debe contener al menos el periodo escolar que acabamos de insertar");
    }

    @Test
    void testUpdateSchoolPeriodSuccess() throws DAOException {
        int generatedId = periodDAOTest.insertSchoolPeriod(testPeriod);

        testPeriod.setPeriodName("Agosto - Enero 2027");
        testPeriod.setStatus("activo");
        testPeriod.setStartDate(LocalDate.of(2026, 8, 15));

        periodDAOTest.updateSchoolPeriod(testPeriod, generatedId);

        SchoolPeriod recovered = periodDAOTest.recoverSchoolPeriod(generatedId);
        assertEquals(testPeriod, recovered, "Los datos del periodo escolar recuperado no reflejan la actualización.");
    }
}