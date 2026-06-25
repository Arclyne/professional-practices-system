package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.MonthlyReport;
import java.util.List;

public interface IMonthlyReportDAO {
    int insertReport(MonthlyReport report) throws DAOException;
    void updateReport(MonthlyReport report, int reportId) throws DAOException;
    List<MonthlyReport> getReportsByPractitioner(int practitionerId) throws DAOException;
    MonthlyReport getReportById(int reportId) throws DAOException;
    List<MonthlyReport> getSubmittedReports() throws DAOException;
    List<MonthlyReport> getSubmittedReportsByProfessor(int professorId, int periodId) throws DAOException;
    List<MonthlyReport> getReportsByPractitionerInRange(int practitionerId, java.sql.Date startDate,
                                                        java.sql.Date endDate) throws DAOException;
}
