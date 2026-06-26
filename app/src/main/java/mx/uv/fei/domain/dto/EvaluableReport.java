package mx.uv.fei.domain.dto;

import java.util.Objects;


public class EvaluableReport {

    public static final String KIND_MONTHLY      = "Mensual";
    public static final String KIND_INTERMEDIATE = "Intermedio";
    public static final String KIND_FINAL        = "Final";

    private int reportId;
    private int practitionerId;
    private String practitionerEnrollment;
    private String groupSection;
    private String reportKind;
    private String displayName;
    private String status;
    private String signedFileUrl;
    private Double grade;
    private String professorFeedback;

    public EvaluableReport() {}

    public static EvaluableReport fromMonthlyReport(MonthlyReport source) {
        EvaluableReport evaluableReport = new EvaluableReport();
        evaluableReport.setReportId(source.getReportId());
        evaluableReport.setPractitionerId(source.getPractitionerId());
        evaluableReport.setReportKind(KIND_MONTHLY);
        evaluableReport.setDisplayName("[Mensual] " + source.getMonthName() + " " + source.getYear());
        evaluableReport.setStatus(source.getStatus());
        evaluableReport.setSignedFileUrl(source.getSignedFileUrl());
        evaluableReport.setGrade(source.getGrade());
        evaluableReport.setProfessorFeedback(source.getProfessorFeedback());
        return evaluableReport;
    }

    public static EvaluableReport fromProgressReport(ProgressReport source) {
        EvaluableReport evaluableReport = new EvaluableReport();
        evaluableReport.setReportId(source.getReportId());
        evaluableReport.setPractitionerId(source.getPractitionerId());
        evaluableReport.setReportKind(source.getReportType());
        evaluableReport.setDisplayName("[" + source.getReportType() + "] "
                + source.getPeriodCoveredStart() + " al " + source.getPeriodCoveredEnd()
                + " — " + source.getTotalHoursAtSubmission() + " hrs");
        evaluableReport.setStatus(source.getStatus());
        evaluableReport.setSignedFileUrl(source.getSignedFileUrl());
        evaluableReport.setGrade(source.getGrade());
        evaluableReport.setProfessorFeedback(source.getProfessorFeedback());
        return evaluableReport;
    }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getPractitionerId() { return practitionerId; }
    public void setPractitionerId(int practitionerId) { this.practitionerId = practitionerId; }

    public String getPractitionerEnrollment() { return practitionerEnrollment; }
    public void setPractitionerEnrollment(String practitionerEnrollment) {
        this.practitionerEnrollment = practitionerEnrollment;
    }

    public String getGroupSection() { return groupSection; }
    public void setGroupSection(String groupSection) { this.groupSection = groupSection; }

    public String getReportKind() { return reportKind; }
    public void setReportKind(String reportKind) { this.reportKind = reportKind; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSignedFileUrl() { return signedFileUrl; }
    public void setSignedFileUrl(String signedFileUrl) { this.signedFileUrl = signedFileUrl; }

    public Double getGrade() { return grade; }
    public void setGrade(Double grade) { this.grade = grade; }

    public String getProfessorFeedback() { return professorFeedback; }
    public void setProfessorFeedback(String professorFeedback) { this.professorFeedback = professorFeedback; }

    public boolean isProgressReport() {
        return KIND_INTERMEDIATE.equals(reportKind) || KIND_FINAL.equals(reportKind);
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            EvaluableReport other = (EvaluableReport) obj;
            isEqual = this.reportId == other.reportId &&
                    Objects.equals(this.reportKind, other.reportKind);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportId, reportKind);
    }

    @Override
    public String toString() {
        return displayName;
    }
}