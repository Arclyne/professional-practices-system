package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProgressReportDAO;
import mx.uv.fei.domain.dto.ProgressReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class ProgressReportDAOTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int PROFESSOR_ID = 68;
    private static final int PERIOD_ID = 5;
    private static final int SEED_REPORT_ID = 1;
    private static final int SEED_MONTHLY_REPORT_ID = 1;
    private static final int NON_EXISTENT_ID = 9999;
    private static final double SEED_EVALUATED_HOURS = 5.0;
    private static final double NO_HOURS = 0.0;
    private static final Date RANGE_START = Date.valueOf("2026-05-01");
    private static final Date RANGE_END = Date.valueOf("2026-05-31");
    private static final Date OUT_OF_RANGE_START = Date.valueOf("2026-07-01");
    private static final Date OUT_OF_RANGE_END = Date.valueOf("2026-07-31");

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IProgressReportDAO progressReportDAO;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void getSubmittedProgressReportsByProfessor_OwnPractitioner_ReturnsReport() throws DAOException, SQLException {
        markSeedReportAsSubmitted();

        List<ProgressReport> resultReports = progressReportDAO
                .getSubmittedProgressReportsByProfessor(PROFESSOR_ID, PERIOD_ID);

        assertEquals(1, resultReports.size());
        assertEquals(PRACTITIONER_ID, resultReports.get(0).getPractitionerId());
    }

    @Test
    void getSubmittedProgressReportsByProfessor_OtherProfessor_ReturnsEmptyList() throws DAOException, SQLException {
        markSeedReportAsSubmitted();

        List<ProgressReport> resultReports = progressReportDAO
                .getSubmittedProgressReportsByProfessor(NON_EXISTENT_ID, PERIOD_ID);

        assertTrue(resultReports.isEmpty());
    }

    @Test
    void getTotalAccumulatedHours_MonthlyReportNotEvaluated_ReturnsZero() throws DAOException {
        double accumulatedHours = progressReportDAO.getTotalAccumulatedHours(PRACTITIONER_ID);

        assertEquals(NO_HOURS, accumulatedHours);
    }

    @Test
    void getTotalAccumulatedHours_MonthlyReportEvaluated_ReturnsLinkedHours() throws DAOException, SQLException {
        markSeedMonthlyReportAsEvaluated();

        double accumulatedHours = progressReportDAO.getTotalAccumulatedHours(PRACTITIONER_ID);

        assertEquals(SEED_EVALUATED_HOURS, accumulatedHours);
    }

    @Test
    void getAccumulatedHoursInRange_EvaluatedReportInsideRange_ReturnsLinkedHours() throws DAOException, SQLException {
        markSeedMonthlyReportAsEvaluated();

        double accumulatedHours = progressReportDAO.getAccumulatedHoursInRange(PRACTITIONER_ID, RANGE_START, RANGE_END);

        assertEquals(SEED_EVALUATED_HOURS, accumulatedHours);
    }

    @Test
    void getAccumulatedHoursInRange_EvaluatedReportOutsideRange_ReturnsZero() throws DAOException, SQLException {
        markSeedMonthlyReportAsEvaluated();

        double accumulatedHours = progressReportDAO.getAccumulatedHoursInRange(
                PRACTITIONER_ID, OUT_OF_RANGE_START, OUT_OF_RANGE_END);

        assertEquals(NO_HOURS, accumulatedHours);
    }

    private void markSeedReportAsSubmitted() throws SQLException {
        String updateStatus = "UPDATE progress_report SET status = 'Entregado' WHERE report_id = " + SEED_REPORT_ID;

        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(updateStatus);
        }
    }

    private void markSeedMonthlyReportAsEvaluated() throws SQLException {
        String updateStatus = "UPDATE monthly_report SET status = 'Evaluado' WHERE report_id = " + SEED_MONTHLY_REPORT_ID;

        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(updateStatus);
        }
    }
}
