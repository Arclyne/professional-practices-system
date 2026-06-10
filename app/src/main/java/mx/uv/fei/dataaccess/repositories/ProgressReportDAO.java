package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProgressReportDAO;
import mx.uv.fei.domain.dto.ProgressReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

@Component
public class ProgressReportDAO extends BaseDAO implements IProgressReportDAO {

    private static final String DEFAULT_REPORT_STATUS = "Pendiente de Firma";

    private static final String SQL_INSERT_PROGRESS_REPORT =
            "INSERT INTO progress_report (practitioner_id, report_type, generation_date, " +
                    "period_covered_start, period_covered_end, total_hours_at_submission, status, signed_file_url) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_PROGRESS_REPORT =
            "UPDATE progress_report SET status = ?, signed_file_url = ?, grade = ?, " +
                    "professor_feedback = ? WHERE report_id = ?";
    private static final String SQL_SELECT_PROGRESS_REPORT_BY_PRACTITIONER_AND_TYPE =
            "SELECT * FROM progress_report WHERE practitioner_id = ? AND report_type = ?";
    private static final String SQL_SELECT_PROGRESS_REPORTS_BY_PRACTITIONER =
            "SELECT * FROM progress_report WHERE practitioner_id = ? ORDER BY generation_date DESC";
    private static final String SQL_SELECT_SUBMITTED_PROGRESS_REPORTS =
            "SELECT * FROM progress_report WHERE status IN ('Entregado', 'Evaluado') " +
                    "ORDER BY generation_date DESC";
    private static final String SQL_SUM_ACCUMULATED_HOURS =
            "SELECT COALESCE(SUM(a.duration_hours), 0) " +
                    "FROM activity a " +
                    "INNER JOIN monthly_report mr ON a.report_id = mr.report_id " +
                    "WHERE mr.practitioner_id = ?";

    @Inject
    public ProgressReportDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertProgressReport(ProgressReport progressReport) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PROGRESS_REPORT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, progressReport.getPractitionerId());
            statement.setString(2, progressReport.getReportType());
            statement.setDate(3, progressReport.getGenerationDate());
            statement.setDate(4, progressReport.getPeriodCoveredStart());
            statement.setDate(5, progressReport.getPeriodCoveredEnd());
            statement.setDouble(6, progressReport.getTotalHoursAtSubmission());
            statement.setString(7, progressReport.getStatus() != null ? progressReport.getStatus() : DEFAULT_REPORT_STATUS);
            statement.setString(8, progressReport.getSignedFileUrl());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al registrar el reporte de avance.", e);
        }

        return generatedId;
    }

    @Override
    public boolean updateProgressReport(ProgressReport progressReport, int reportId) throws DAOException {
        return updateTuple(SQL_UPDATE_PROGRESS_REPORT, statement -> {
            statement.setString(1, progressReport.getStatus());
            statement.setString(2, progressReport.getSignedFileUrl());
            statement.setObject(3, progressReport.getGrade(), Types.DECIMAL);
            statement.setString(4, progressReport.getProfessorFeedback());
            statement.setInt(5, reportId);
        });
    }

    @Override
    public ProgressReport getProgressReportByPractitionerAndType(int practitionerId, String reportType) throws DAOException {
        ProgressReport recoveredReport = new ProgressReport();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_PROGRESS_REPORT_BY_PRACTITIONER_AND_TYPE)) {

            statement.setInt(1, practitionerId);
            statement.setString(2, reportType);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredReport = mapResultSetToProgressReport(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar el reporte de avance.", e);
        }

        return recoveredReport;
    }

    @Override
    public List<ProgressReport> getProgressReportsByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_PROGRESS_REPORTS_BY_PRACTITIONER, this::mapResultSetToProgressReport, practitionerId);
    }

    @Override
    public List<ProgressReport> getSubmittedProgressReports() throws DAOException {
        return recoverALL(SQL_SELECT_SUBMITTED_PROGRESS_REPORTS, this::mapResultSetToProgressReport);
    }

    @Override
    public double getTotalAccumulatedHours(int practitionerId) throws DAOException {
        double accumulatedHours = 0.0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SUM_ACCUMULATED_HOURS)) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    accumulatedHours = resultSet.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al calcular las horas acumuladas.", e);
        }

        return accumulatedHours;
    }

    private ProgressReport mapResultSetToProgressReport(ResultSet resultSet) throws SQLException {
        ProgressReport progressReport = new ProgressReport();
        progressReport.setReportId(resultSet.getInt("report_id"));
        progressReport.setPractitionerId(resultSet.getInt("practitioner_id"));
        progressReport.setReportType(resultSet.getString("report_type"));
        progressReport.setGenerationDate(resultSet.getDate("generation_date"));
        progressReport.setPeriodCoveredStart(resultSet.getDate("period_covered_start"));
        progressReport.setPeriodCoveredEnd(resultSet.getDate("period_covered_end"));
        progressReport.setTotalHoursAtSubmission(resultSet.getDouble("total_hours_at_submission"));
        progressReport.setStatus(resultSet.getString("status"));
        progressReport.setSignedFileUrl(resultSet.getString("signed_file_url"));
        progressReport.setGrade(resolveNullableGrade(resultSet));
        progressReport.setProfessorFeedback(resultSet.getString("professor_feedback"));
        return progressReport;
    }

    private Double resolveNullableGrade(ResultSet resultSet) throws SQLException {
        double grade = resultSet.getDouble("grade");
        return resultSet.wasNull() ? null : grade;
    }
}