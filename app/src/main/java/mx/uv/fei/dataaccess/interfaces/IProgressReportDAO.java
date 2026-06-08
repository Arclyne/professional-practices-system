package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.ProgressReport;
import java.util.List;

/**
 * Contrato de acceso a datos para los reportes de avance de prácticas
 * (Intermedios y Finales).
 *
 * @author Sistema de Prácticas Profesionales UV-FEI
 */
public interface IProgressReportDAO {

    int insertProgressReport(ProgressReport report) throws DAOException;

    boolean updateProgressReport(ProgressReport report, int reportId) throws DAOException;

    ProgressReport getProgressReportByPractitionerAndType(int practitionerId, String reportType) throws DAOException;

    List<ProgressReport> getProgressReportsByPractitioner(int practitionerId) throws DAOException;

    List<ProgressReport> getSubmittedProgressReports() throws DAOException;

    double getTotalAccumulatedHours(int practitionerId) throws DAOException;
}
