package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IMonthlyReportDAO;
import mx.uv.fei.domain.dto.MonthlyReport;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

/**
 * Acceso a datos de los reportes mensuales de los practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class MonthlyReportDAO extends BaseDAO implements IMonthlyReportDAO {

    private static final String DEFAULT_REPORT_STATUS = "Pendiente de Firma";

    private static final String SQL_INSERT_REPORT =
            "INSERT INTO monthly_report (practitioner_id, month_name, year, start_date, end_date, status, signed_file_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_REPORT =
            "UPDATE monthly_report SET month_name = ?, year = ?, start_date = ?, end_date = ?, grade = ?, professor_feedback = ?, status = ?, signed_file_url = ? WHERE report_id = ?";
    private static final String SQL_SELECT_REPORT_BY_ID =
            "SELECT * FROM monthly_report WHERE report_id = ?";
    private static final String SQL_SELECT_REPORTS_BY_PRACTITIONER =
            "SELECT * FROM monthly_report WHERE practitioner_id = ? ORDER BY year DESC, start_date DESC";
    private static final String SQL_SELECT_SUBMITTED_REPORTS =
            "SELECT * FROM monthly_report WHERE status IN ('Entregado', 'Evaluado') ORDER BY year DESC, start_date DESC";
    private static final String SQL_SELECT_SUBMITTED_REPORTS_BY_PROFESSOR =
            "SELECT mr.* FROM monthly_report mr " +
                    "INNER JOIN group_enrollment ge ON mr.practitioner_id = ge.practitioner_id " +
                    "INNER JOIN practice_group pg ON ge.group_id = pg.group_id " +
                    "WHERE mr.status IN ('Entregado', 'Evaluado') " +
                    "AND pg.professor_id = ? " +
                    "AND pg.period_id = ? " +
                    "ORDER BY mr.year DESC, mr.start_date DESC";
    private static final String SQL_SELECT_REPORTS_BY_PRACTITIONER_IN_RANGE =
            "SELECT * FROM monthly_report " +
                    "WHERE practitioner_id = ? AND start_date >= ? AND end_date <= ? " +
                    "ORDER BY start_date ASC";

    /**
     * Crea el DAO de reportes mensuales con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public MonthlyReportDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta un reporte mensual con estado pendiente por defecto y devuelve su identificador generado.
     *
     * @param report reporte con los datos a registrar
     * @return identificador generado para el reporte, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar el reporte
     */
    @Override
    public int insertReport(MonthlyReport report) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_REPORT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, report.getPractitionerId());
            statement.setString(2, report.getMonthName());
            statement.setInt(3, report.getYear());
            statement.setDate(4, report.getStartDate());
            statement.setDate(5, report.getEndDate());
            statement.setString(6, report.getStatus() != null ? report.getStatus() : DEFAULT_REPORT_STATUS);
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

    /**
     * Actualiza los datos de un reporte mensual, incluyendo calificación y retroalimentación.
     *
     * @param report   reporte con los datos modificados
     * @param reportId identificador del reporte a actualizar
     * @throws DAOException si el reporte no existe o si ocurre un error al actualizar
     */
    @Override
    public void updateReport(MonthlyReport report, int reportId) throws DAOException {
        updateTuple(SQL_UPDATE_REPORT, statement -> {
            statement.setString(1, report.getMonthName());
            statement.setInt(2, report.getYear());
            statement.setDate(3, report.getStartDate());
            statement.setDate(4, report.getEndDate());
            statement.setObject(5, report.getGrade(), Types.DECIMAL);
            statement.setString(6, report.getProfessorFeedback());
            statement.setString(7, report.getStatus());
            statement.setString(8, report.getSignedFileUrl());
            statement.setInt(9, reportId);
        });
    }

    /**
     * Recupera un reporte mensual a partir de su identificador.
     *
     * @param reportId identificador del reporte a recuperar
     * @return reporte encontrado, o un {@link MonthlyReport} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public MonthlyReport getReportById(int reportId) throws DAOException {
        MonthlyReport recoveredReport = new MonthlyReport();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_REPORT_BY_ID)) {

            statement.setInt(1, reportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredReport = mapResultSetToMonthlyReport(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar el reporte de la base de datos.", e);
        }

        return recoveredReport;
    }

    /**
     * Recupera los reportes mensuales de un practicante, del más reciente al más antiguo.
     *
     * @param practitionerId identificador del practicante
     * @return lista de reportes del practicante; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<MonthlyReport> getReportsByPractitioner(int practitionerId) throws DAOException {
        return recoverALL(SQL_SELECT_REPORTS_BY_PRACTITIONER, this::mapResultSetToMonthlyReport, practitionerId);
    }

    /**
     * Recupera todos los reportes mensuales entregados o evaluados.
     *
     * @return lista de reportes entregados; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<MonthlyReport> getSubmittedReports() throws DAOException {
        return recoverALL(SQL_SELECT_SUBMITTED_REPORTS, this::mapResultSetToMonthlyReport);
    }

    /**
     * Recupera los reportes entregados de los practicantes a cargo de un profesor en un periodo.
     *
     * @param professorId identificador del profesor
     * @param periodId    identificador del periodo escolar
     * @return lista de reportes entregados; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<MonthlyReport> getSubmittedReportsByProfessor(int professorId, int periodId) throws DAOException {
        return recoverALL(SQL_SELECT_SUBMITTED_REPORTS_BY_PROFESSOR, this::mapResultSetToMonthlyReport,
                professorId, periodId);
    }

    /**
     * Recupera los reportes de un practicante cuyas fechas caen dentro de un rango.
     *
     * @param practitionerId identificador del practicante
     * @param startDate      fecha de inicio del rango (inclusive)
     * @param endDate        fecha de fin del rango (inclusive)
     * @return lista de reportes dentro del rango, ordenados por fecha de inicio; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<MonthlyReport> getReportsByPractitionerInRange(int practitionerId, java.sql.Date startDate,
                                                               java.sql.Date endDate) throws DAOException {
        return recoverALL(SQL_SELECT_REPORTS_BY_PRACTITIONER_IN_RANGE, this::mapResultSetToMonthlyReport,
                practitionerId, startDate, endDate);
    }

    /**
     * Construye un reporte mensual con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return reporte mensual con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private MonthlyReport mapResultSetToMonthlyReport(ResultSet resultSet) throws SQLException {
        MonthlyReport report = new MonthlyReport();
        report.setReportId(resultSet.getInt("report_id"));
        report.setPractitionerId(resultSet.getInt("practitioner_id"));
        report.setMonthName(resultSet.getString("month_name"));
        report.setYear(resultSet.getInt("year"));
        report.setStartDate(resultSet.getDate("start_date"));
        report.setEndDate(resultSet.getDate("end_date"));
        report.setGrade(resolveNullableGrade(resultSet));
        report.setProfessorFeedback(resultSet.getString("professor_feedback"));
        report.setStatus(resultSet.getString("status"));
        report.setSignedFileUrl(resultSet.getString("signed_file_url"));
        return report;
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