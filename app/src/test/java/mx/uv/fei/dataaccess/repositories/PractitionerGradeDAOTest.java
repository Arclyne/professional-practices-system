package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerGradeDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PractitionerGradeDAOTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int SEED_MONTHLY_REPORT_ID = 1;
    private static final int SEED_PROGRESS_REPORT_ID = 1;
    private static final double NO_GRADE = 0.0;
    private static final double MONTHLY_GRADE = 8.0;
    private static final double PROGRESS_GRADE = 10.0;
    private static final double AVERAGE_GRADE = 9.0;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPractitionerGradeDAO practitionerGradeDAO;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void calculateTentativeGrade_NoEvaluatedGrades_ReturnsZero() throws DAOException {
        double tentativeGrade = practitionerGradeDAO.calculateTentativeGrade(PRACTITIONER_ID);

        assertEquals(NO_GRADE, tentativeGrade);
    }

    @Test
    void calculateTentativeGrade_OnlyMonthlyReportEvaluated_ReturnsMonthlyGrade() throws DAOException, SQLException {
        evaluateMonthlyReport(MONTHLY_GRADE);

        double tentativeGrade = practitionerGradeDAO.calculateTentativeGrade(PRACTITIONER_ID);

        assertEquals(MONTHLY_GRADE, tentativeGrade);
    }

    @Test
    void calculateTentativeGrade_MonthlyAndProgressEvaluated_ReturnsAverage() throws DAOException, SQLException {
        evaluateMonthlyReport(MONTHLY_GRADE);
        evaluateProgressReport(PROGRESS_GRADE);

        double tentativeGrade = practitionerGradeDAO.calculateTentativeGrade(PRACTITIONER_ID);

        assertEquals(AVERAGE_GRADE, tentativeGrade);
    }

    private void evaluateMonthlyReport(double grade) throws SQLException {
        executeUpdate("UPDATE monthly_report SET status = 'Evaluado', grade = " + grade
                + " WHERE report_id = " + SEED_MONTHLY_REPORT_ID);
    }

    private void evaluateProgressReport(double grade) throws SQLException {
        executeUpdate("UPDATE progress_report SET status = 'Evaluado', grade = " + grade
                + " WHERE report_id = " + SEED_PROGRESS_REPORT_ID);
    }

    private void executeUpdate(String updateStatement) throws SQLException {
        try (Connection connection = dbConnection.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(updateStatement);
        }
    }
}
