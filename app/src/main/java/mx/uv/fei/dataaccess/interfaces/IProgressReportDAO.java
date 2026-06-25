package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.ProgressReport;
import java.util.List;

public interface IProgressReportDAO {

    int insertProgressReport(ProgressReport report) throws DAOException;

    void updateProgressReport(ProgressReport report, int reportId) throws DAOException;

    ProgressReport getProgressReportByPractitionerAndType(int practitionerId, String reportType) throws DAOException;

    List<ProgressReport> getProgressReportsByPractitioner(int practitionerId) throws DAOException;

    List<ProgressReport> getSubmittedProgressReports() throws DAOException;

    List<ProgressReport> getSubmittedProgressReportsByProfessor(int professorId, int periodId) throws DAOException;

    double getTotalAccumulatedHours(int practitionerId) throws DAOException;

    double getAccumulatedHoursInRange(int practitionerId, java.sql.Date startDate, java.sql.Date endDate) throws DAOException;
}
