package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;

public class MonthlyReport {
    private int reportId;
    private int practitionerId;
    private String monthName;
    private int year;
    private Date startDate;
    private Date endDate;
    private Double grade;
    private String professorFeedback;
    private String status;
    private String signedFileUrl;

    public MonthlyReport() {}

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getPractitionerId() { return practitionerId; }
    public void setPractitionerId(int practitionerId) { this.practitionerId = practitionerId; }

    public String getMonthName() { return monthName; }
    public void setMonthName(String monthName) { this.monthName = monthName; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }

    public String getProfessorFeedback() { return professorFeedback; }
    public void setProfessorFeedback(String professorFeedback) { this.professorFeedback = professorFeedback; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSignedFileUrl() { return signedFileUrl; }
    public void setSignedFileUrl(String signedFileUrl) { this.signedFileUrl = signedFileUrl; }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;
        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            MonthlyReport that = (MonthlyReport) object;
            isEqual = reportId == that.reportId &&
                    practitionerId == that.practitionerId &&
                    year == that.year &&
                    Objects.equals(monthName, that.monthName) &&
                    Objects.equals(startDate, that.startDate) &&
                    Objects.equals(endDate, that.endDate) &&
                    Objects.equals(grade, that.grade) &&
                    Objects.equals(professorFeedback, that.professorFeedback) &&
                    Objects.equals(status, that.status);
                    Objects.equals(signedFileUrl, that.signedFileUrl);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, practitionerId, monthName, year, startDate, endDate, grade, professorFeedback, status, signedFileUrl);
    }
}