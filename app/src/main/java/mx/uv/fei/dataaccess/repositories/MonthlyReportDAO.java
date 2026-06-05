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
import mx.uv.fei.dataaccess.interfaces.IMonthlyReportDAO;
import mx.uv.fei.domain.dto.MonthlyReport;

@Component
public class MonthlyReportDAO extends BaseDAO implements IMonthlyReportDAO {

    private static final String SQL_SELECT_SUBMITTED = "SELECT * FROM monthly_report WHERE status IN ('Entregado', 'Evaluado') ORDER BY year DESC, start_date DESC";
    private static final String SQL_INSERT = "INSERT INTO monthly_report (practitioner_id, month_name, year, start_date, end_date, status, signed_file_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE monthly_report SET month_name = ?, year = ?, start_date = ?, end_date = ?, grade = ?, professor_feedback = ?, status = ?, signed_file_url = ? WHERE report_id = ?";
    private static final String SQL_SELECT_BY_PRACTITIONER = "SELECT * FROM monthly_report WHERE practitioner_id = ? ORDER BY year DESC, start_date DESC";
    private static final String SQL_SELECT_BY_ID = "SELECT * FROM monthly_report WHERE report_id = ?";

    @Inject
    public MonthlyReportDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertReport(MonthlyReport report) throws DAOException {
        int generatedId = -1;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, report.getPractitionerId());
            statement.setString(2, report.getMonthName());
            statement.setInt(3, report.getYear());
            statement.setDate(4, report.getStartDate());
            statement.setDate(5, report.getEndDate());
            statement.setString(6, report.getStatus() != null ? report.getStatus() : "Pendiente de Firma");
            statement.setString(7, report.getSignedFileUrl());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al crear el reporte mensual.", e);
        }
        return generatedId;
    }

    @Override
    public boolean updateReport(MonthlyReport report, int reportId) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, report.getMonthName());
            statement.setInt(2, report.getYear());
            statement.setDate(3, report.getStartDate());
            statement.setDate(4, report.getEndDate());

            if (report.getGrade() != null) {
                statement.setDouble(5, report.getGrade());
            } else {
                statement.setNull(5, java.sql.Types.DECIMAL);
            }

            // ...
            statement.setString(6, report.getProfessorFeedback());
            statement.setString(7, report.getStatus());
            statement.setString(8, report.getSignedFileUrl());
            statement.setInt(9, reportId);
        });
    }

    @Override
    public List<MonthlyReport> getReportsByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_BY_PRACTITIONER, this::mapResultSetToReport, practitionerId);
    }

    @Override
    public List<MonthlyReport> getSubmittedReports() throws DAOException {
        return recoverALL(SQL_SELECT_SUBMITTED, this::mapResultSetToReport);
    }

    @Override
    public MonthlyReport getReportById(int reportId) throws DAOException {
        MonthlyReport report = new MonthlyReport();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {
            statement.setInt(1, reportId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    report = mapResultSetToReport(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar el reporte de la base de datos.", e);
        }
        return report;
    }

    private MonthlyReport mapResultSetToReport(ResultSet resultSet) throws SQLException {
        MonthlyReport report = new MonthlyReport();
        report.setReportId(resultSet.getInt("report_id"));
        report.setPractitionerId(resultSet.getInt("practitioner_id"));
        report.setMonthName(resultSet.getString("month_name"));
        report.setYear(resultSet.getInt("year"));
        report.setStartDate(resultSet.getDate("start_date"));
        report.setEndDate(resultSet.getDate("end_date"));

        double grade = resultSet.getDouble("grade");
        report.setGrade(resultSet.wasNull() ? null : grade);

        report.setProfessorFeedback(resultSet.getString("professor_feedback"));
        report.setStatus(resultSet.getString("status"));
        report.setSignedFileUrl(resultSet.getString("signed_file_url"));

        return report;
    }
}