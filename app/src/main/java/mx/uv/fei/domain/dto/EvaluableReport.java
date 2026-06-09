package mx.uv.fei.domain.dto;

import java.util.Objects;


public class EvaluableReport {

    public static final String KIND_MONTHLY      = "Mensual";
    public static final String KIND_INTERMEDIATE = "Intermedio";
    public static final String KIND_FINAL        = "Final";

    private int reportId;
    private int practitionerId;
    private String reportKind;
    private String displayName;
    private String status;
    private String signedFileUrl;
    private Double grade;
    private String professorFeedback;

    public EvaluableReport() {
    }

    public static EvaluableReport fromMonthlyReport(MonthlyReport source) {
        EvaluableReport item = new EvaluableReport();
        item.setReportId(source.getReportId());
        item.setPractitionerId(source.getPractitionerId());
        item.setReportKind(KIND_MONTHLY);
        item.setDisplayName("[Mensual] " + source.getMonthName() + " " + source.getYear()
                + " — Practicante " + source.getPractitionerId());
        item.setStatus(source.getStatus());
        item.setSignedFileUrl(source.getSignedFileUrl());
        item.setGrade(source.getGrade());
        item.setProfessorFeedback(source.getProfessorFeedback());
        return item;
    }

    public static EvaluableReport fromProgressReport(ProgressReport source) {
        EvaluableReport item = new EvaluableReport();
        item.setReportId(source.getReportId());
        item.setPractitionerId(source.getPractitionerId());
        item.setReportKind(source.getReportType());
        item.setDisplayName("[" + source.getReportType() + "] "
                + source.getPeriodCoveredStart() + " al " + source.getPeriodCoveredEnd()
                + " — " + source.getTotalHoursAtSubmission() + " hrs"
                + " — Practicante " + source.getPractitionerId());
        item.setStatus(source.getStatus());
        item.setSignedFileUrl(source.getSignedFileUrl());
        item.setGrade(source.getGrade());
        item.setProfessorFeedback(source.getProfessorFeedback());
        return item;
    }

    // ── Getters y setters ──────────────────────────────────────────────────────

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

    public String getReportKind() {
        return reportKind;
    }

    public void setReportKind(String reportKind) {
        this.reportKind = reportKind;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    /**
     * Indica si este reporte es de tipo progreso (Intermedio o Final).
     *
     * @return true si el kind es "Intermedio" o "Final"
     */
    public boolean isProgressReport() {
        return KIND_INTERMEDIATE.equals(reportKind) || KIND_FINAL.equals(reportKind);
    }

    /**
     * Business Key: un reporte es único por ID y tipo.
     */
    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            EvaluableReport that = (EvaluableReport) object;
            isEqual = this.reportId == that.reportId
                    && Objects.equals(this.reportKind, that.reportKind);
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