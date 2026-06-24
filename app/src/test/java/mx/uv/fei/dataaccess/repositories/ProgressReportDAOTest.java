package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
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
    private static final int NON_EXISTENT_ID = 9999;

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

    private void markSeedReportAsSubmitted() throws SQLException {
        String updateStatus = "UPDATE progress_report SET status = 'Entregado' WHERE report_id = " + SEED_REPORT_ID;

        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(updateStatus);
        }
    }
}
