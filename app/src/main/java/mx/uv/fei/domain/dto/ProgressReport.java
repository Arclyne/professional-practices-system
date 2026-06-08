package mx.uv.fei.domain.dto;

import java.sql.Date;
import java.util.Objects;


public class ProgressReport {

    private int reportId;
    private int practitionerId;
    private String reportType;
    private Date generationDate;
    private Date periodCoveredStart;
    private Date periodCoveredEnd;
    private double totalHoursAtSubmission;
    private String status;
    private String signedFileUrl;
    private Double grade;
    private String professorFeedback;

    public ProgressReport() {
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public int getPractitionerId() {
        return practitionerId;
    }

    public void setPractitionerId(int practitionerId) {
        this.practitionerId = practitionerId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public Date getGenerationDate() {
        return generationDate;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    public Date getPeriodCoveredStart() {
        return periodCoveredStart;
    }

    public void setPeriodCoveredStart(Date periodCoveredStart) {
        this.periodCoveredStart = periodCoveredStart;
    }

    public Date getPeriodCoveredEnd() {
        return periodCoveredEnd;
    }

    public void setPeriodCoveredEnd(Date periodCoveredEnd) {
        this.periodCoveredEnd = periodCoveredEnd;
    }

    public double getTotalHoursAtSubmission() {
        return totalHoursAtSubmission;
    }

    public void setTotalHoursAtSubmission(double totalHoursAtSubmission) {
        this.totalHoursAtSubmission = totalHoursAtSubmission;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSignedFileUrl() {
        return signedFileUrl;
    }

    public void setSignedFileUrl(String signedFileUrl) {
        this.signedFileUrl = signedFileUrl;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public String getProfessorFeedback() {
        return professorFeedback;
    }

    public void setProfessorFeedback(String professorFeedback) {
        this.professorFeedback = professorFeedback;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            ProgressReport that = (ProgressReport) object;
            isEqual = this.practitionerId == that.practitionerId
                    && Objects.equals(this.reportType, that.reportType);
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(practitionerId, reportType);
    }
}
