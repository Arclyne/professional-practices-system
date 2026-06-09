package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.MonthlyReport;
import java.util.List;

public interface IMonthlyReportDAO {
    int insertReport(MonthlyReport report) throws DAOException;
    boolean updateReport(MonthlyReport report, int reportId) throws DAOException;
    List<MonthlyReport> getReportsByPractitioner(int practitionerId) throws DAOException;
    MonthlyReport getReportById(int reportId) throws DAOException;
    List<MonthlyReport> getSubmittedReports() throws DAOException;
}
