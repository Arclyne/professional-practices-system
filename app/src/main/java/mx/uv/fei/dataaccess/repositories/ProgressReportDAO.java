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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Acceso a datos de los reportes de avance, intermedios y finales.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class ProgressReportDAO extends BaseDAO implements IProgressReportDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProgressReportDAO.class);
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
    private static final String SQL_SELECT_SUBMITTED_PROGRESS_REPORTS_BY_PROFESSOR =
            "SELECT pr.* FROM progress_report pr " +
                    "INNER JOIN group_enrollment ge ON pr.practitioner_id = ge.practitioner_id " +
                    "INNER JOIN practice_group pg ON ge.group_id = pg.group_id " +
                    "WHERE pr.status IN ('Entregado', 'Evaluado') " +
                    "AND pg.professor_id = ? " +
                    "AND pg.period_id = ? " +
                    "ORDER BY pr.generation_date DESC";
    private static final String SQL_SUM_ACCUMULATED_HOURS =
            "SELECT COALESCE(SUM(a.duration_hours), 0) " +
                    "FROM activity a " +
                    "INNER JOIN monthly_report mr ON a.report_id = mr.report_id " +
                    "WHERE mr.practitioner_id = ? AND mr.status = 'Evaluado'";
    private static final String SQL_SUM_ACCUMULATED_HOURS_IN_RANGE =
            "SELECT COALESCE(SUM(a.duration_hours), 0) " +
                    "FROM activity a " +
                    "INNER JOIN monthly_report mr ON a.report_id = mr.report_id " +
                    "WHERE mr.practitioner_id = ? AND mr.status = 'Evaluado' " +
                    "AND mr.start_date >= ? AND mr.end_date <= ?";

    /**
     * Crea el DAO de reportes de avance con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public ProgressReportDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta un reporte de avance con estado pendiente por defecto y devuelve su identificador generado.
     *
     * @param progressReport reporte de avance con los datos a registrar
     * @return identificador generado para el reporte, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar el reporte
     */
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
            logger.error("Error en BD al insertar reporte. SQL State: {}, Error Code: {}", e.getSQLState(), e.getErrorCode(), e);
            throw new DAOException("Error al registrar el reporte de avance.", e);
        }

        return generatedId;
    }

    /**
     * Actualiza el estado, archivo firmado, calificación y retroalimentación de un reporte de avance.
     *
     * @param progressReport reporte con los datos modificados
     * @param reportId       identificador del reporte a actualizar
     * @throws DAOException si el reporte no existe o si ocurre un error al actualizar
     */
    @Override
    public void updateProgressReport(ProgressReport progressReport, int reportId) throws DAOException {
        updateTuple(SQL_UPDATE_PROGRESS_REPORT, statement -> {
            statement.setString(1, progressReport.getStatus());
            statement.setString(2, progressReport.getSignedFileUrl());
            statement.setObject(3, progressReport.getGrade(), Types.DECIMAL);
            statement.setString(4, progressReport.getProfessorFeedback());
            statement.setInt(5, reportId);
        });
    }

    /**
     * Recupera el reporte de avance de un practicante de un tipo determinado.
     *
     * @param practitionerId identificador del practicante
     * @param reportType     tipo de reporte (por ejemplo intermedio o final)
     * @return reporte encontrado, o {@code null} si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public ProgressReport getProgressReportByPractitionerAndType(int practitionerId, String reportType) throws DAOException {
        ProgressReport recoveredReport = null;

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
            logger.error("Error en BD al insertar reporte. SQL State: {}, Error Code: {}", e.getSQLState(), e.getErrorCode(), e);
            throw new DAOException("Error al recuperar el reporte de avance.", e);
        }

        return recoveredReport;
    }

    /**
     * Recupera los reportes de avance de un practicante, del más reciente al más antiguo.
     *
     * @param practitionerId identificador del practicante
     * @return lista de reportes del practicante; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<ProgressReport> getProgressReportsByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_PROGRESS_REPORTS_BY_PRACTITIONER, this::mapResultSetToProgressReport, practitionerId);
    }

    /**
     * Recupera todos los reportes de avance entregados o evaluados.
     *
     * @return lista de reportes entregados; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<ProgressReport> getSubmittedProgressReports() throws DAOException {
        return recoverALL(SQL_SELECT_SUBMITTED_PROGRESS_REPORTS, this::mapResultSetToProgressReport);
    }

    /**
     * Recupera los reportes de avance entregados de los practicantes a cargo de un profesor en un periodo.
     *
     * @param professorId identificador del profesor
     * @param periodId    identificador del periodo escolar
     * @return lista de reportes entregados; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<ProgressReport> getSubmittedProgressReportsByProfessor(int professorId, int periodId)
            throws DAOException {
        return recoverALL(SQL_SELECT_SUBMITTED_PROGRESS_REPORTS_BY_PROFESSOR, this::mapResultSetToProgressReport,
                professorId, periodId);
    }

    /**
     * Calcula el total de horas acumuladas por un practicante en sus reportes mensuales evaluados.
     *
     * @param practitionerId identificador del practicante
     * @return suma de horas acumuladas, o {@code 0.0} si no hay actividades evaluadas
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Calcula las horas acumuladas por un practicante en los reportes mensuales evaluados de un rango de fechas.
     *
     * @param practitionerId identificador del practicante
     * @param startDate      fecha de inicio del rango (inclusive)
     * @param endDate        fecha de fin del rango (inclusive)
     * @return suma de horas acumuladas en el rango, o {@code 0.0} si no hay actividades evaluadas
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public double getAccumulatedHoursInRange(int practitionerId, java.sql.Date startDate, java.sql.Date endDate)
            throws DAOException {
        double accumulatedHours = 0.0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SUM_ACCUMULATED_HOURS_IN_RANGE)) {

            statement.setInt(1, practitionerId);
            statement.setDate(2, startDate);
            statement.setDate(3, endDate);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    accumulatedHours = resultSet.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al calcular las horas acumuladas del periodo cubierto.", e);
        }

        return accumulatedHours;
    }

    /**
     * Construye un reporte de avance con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return reporte de avance con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
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

    /**
     * Obtiene la calificación del reporte tolerando valores nulos en la columna.
     *
     * @param resultSet resultado posicionado en la fila a leer
     * @return calificación del reporte, o {@code null} si aún no ha sido evaluado
     * @throws SQLException si ocurre un error al leer la columna
     */
    private Double resolveNullableGrade(ResultSet resultSet) throws SQLException {
        double grade = resultSet.getDouble("grade");
        return resultSet.wasNull() ? null : grade;
    }
}