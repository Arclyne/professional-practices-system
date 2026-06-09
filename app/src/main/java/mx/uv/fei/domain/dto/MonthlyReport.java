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
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            MonthlyReport that = (MonthlyReport) obj;
            isEqual = this.practitionerId == that.practitionerId &&
                    this.year == that.year &&
                    Objects.equals(this.monthName, that.monthName);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(practitionerId, monthName, year);
    }
}
