package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IProgressReportDAO;
import mx.uv.fei.domain.dto.ProgressReport;

@Component
public class ProgressReportDAO extends BaseDAO implements IProgressReportDAO {

    private static final String SQL_INSERT =
            "INSERT INTO progress_report (practitioner_id, report_type, generation_date, " +
            "period_covered_start, period_covered_end, total_hours_at_submission, status, signed_file_url) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE progress_report SET status = ?, signed_file_url = ?, grade = ?, " +
            "professor_feedback = ? WHERE report_id = ?";

    private static final String SQL_SELECT_BY_PRACTITIONER_AND_TYPE =
            "SELECT * FROM progress_report WHERE practitioner_id = ? AND report_type = ?";

    private static final String SQL_SELECT_BY_PRACTITIONER =
            "SELECT * FROM progress_report WHERE practitioner_id = ? ORDER BY generation_date DESC";

    private static final String SQL_SELECT_SUBMITTED =
            "SELECT * FROM progress_report WHERE status IN ('Entregado', 'Evaluado') " +
            "ORDER BY generation_date DESC";

    private static final String SQL_SUM_HOURS =
            "SELECT COALESCE(SUM(a.duration_hours), 0) " +
            "FROM activity a " +
            "INNER JOIN monthly_report mr ON a.report_id = mr.report_id " +
            "WHERE mr.practitioner_id = ?";

    private static final String MSG_INSERT_ERROR  = "Error al registrar el reporte de avance.";
    private static final String MSG_UPDATE_ERROR  = "Error al actualizar el reporte de avance.";
    private static final String MSG_SELECT_ERROR  = "Error al recuperar el reporte de avance.";
    private static final String MSG_HOURS_ERROR   = "Error al calcular las horas acumuladas.";

    @Inject
    public ProgressReportDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertProgressReport(ProgressReport report) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, report.getPractitionerId());
            statement.setString(2, report.getReportType());
            statement.setDate(3, report.getGenerationDate());
            statement.setDate(4, report.getPeriodCoveredStart());
            statement.setDate(5, report.getPeriodCoveredEnd());
            statement.setDouble(6, report.getTotalHoursAtSubmission());
            statement.setString(7, report.getStatus() != null ? report.getStatus() : "Pendiente de Firma");
            statement.setString(8, report.getSignedFileUrl());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException(MSG_INSERT_ERROR, e);
        }

        return generatedId;
    }

    @Override
    public boolean updateProgressReport(ProgressReport report, int reportId) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, report.getStatus());
            statement.setString(2, report.getSignedFileUrl());

            if (report.getGrade() != null) {
                statement.setDouble(3, report.getGrade());
            } else {
                statement.setNull(3, java.sql.Types.DECIMAL);
            }

            statement.setString(4, report.getProfessorFeedback());
            statement.setInt(5, reportId);
        });
    }

    @Override
    public ProgressReport getProgressReportByPractitionerAndType(int practitionerId, String reportType) throws DAOException {
        ProgressReport recoveredReport = new ProgressReport();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_PRACTITIONER_AND_TYPE)) {

            statement.setInt(1, practitionerId);
            statement.setString(2, reportType);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredReport = mapResultSetToProgressReport(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException(MSG_SELECT_ERROR, e);
        }

        return recoveredReport;
    }

    @Override
    public List<ProgressReport> getProgressReportsByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_BY_PRACTITIONER, this::mapResultSetToProgressReport, practitionerId);
    }

    @Override
    public List<ProgressReport> getSubmittedProgressReports() throws DAOException {
        return recoverALL(SQL_SELECT_SUBMITTED, this::mapResultSetToProgressReport);
    }

    @Override
    public double getTotalAccumulatedHours(int practitionerId) throws DAOException {
        double accumulatedHours = 0.0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SUM_HOURS)) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    accumulatedHours = resultSet.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException(MSG_HOURS_ERROR, e);
        }

        return accumulatedHours;
    }

    private ProgressReport mapResultSetToProgressReport(ResultSet resultSet) throws SQLException {
        ProgressReport report = new ProgressReport();

        report.setReportId(resultSet.getInt("report_id"));
        report.setPractitionerId(resultSet.getInt("practitioner_id"));
        report.setReportType(resultSet.getString("report_type"));
        report.setGenerationDate(resultSet.getDate("generation_date"));
        report.setPeriodCoveredStart(resultSet.getDate("period_covered_start"));
        report.setPeriodCoveredEnd(resultSet.getDate("period_covered_end"));
        report.setTotalHoursAtSubmission(resultSet.getDouble("total_hours_at_submission"));
        report.setStatus(resultSet.getString("status"));
        report.setSignedFileUrl(resultSet.getString("signed_file_url"));

        double grade = resultSet.getDouble("grade");
        report.setGrade(resultSet.wasNull() ? null : grade);

        report.setProfessorFeedback(resultSet.getString("professor_feedback"));

        return report;
    }
}
