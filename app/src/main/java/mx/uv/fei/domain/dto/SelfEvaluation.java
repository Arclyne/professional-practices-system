package mx.uv.fei.domain.dto;

import java.util.Objects;

/**
 * Representa la autoevaluación de un practicante asociada a un reporte mensual.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class SelfEvaluation {

    private int selfEvalId;
    private int practitionerId;
    private int reportId;
    private String evidence;
    private String status;
    private String reviewComment;
    private int q1;
    private int q2;
    private int q3;
    private int q4;
    private int q5;
    private int q6;
    private int q7;
    private int q8;
    private int q9;
    private int q10;

    public int getSelfEvalId() { return selfEvalId; }
    public void setSelfEvalId(int selfEvalId) { this.selfEvalId = selfEvalId; }

    public int getPractitionerId() { return practitionerId; }
    public void setPractitionerId(int practitionerId) { this.practitionerId = practitionerId; }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public String getEvidence() { return evidence; }
    public void setEvidence(String evidence) { this.evidence = evidence; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

    public int getQ1() { return q1; }
    public void setQ1(int q1) { this.q1 = q1; }

    public int getQ2() { return q2; }
    public void setQ2(int q2) { this.q2 = q2; }

    public int getQ3() { return q3; }
    public void setQ3(int q3) { this.q3 = q3; }

    public int getQ4() { return q4; }
    public void setQ4(int q4) { this.q4 = q4; }

    public int getQ5() { return q5; }
    public void setQ5(int q5) { this.q5 = q5; }

    public int getQ6() { return q6; }
    public void setQ6(int q6) { this.q6 = q6; }

    public int getQ7() { return q7; }
    public void setQ7(int q7) { this.q7 = q7; }

    public int getQ8() { return q8; }
    public void setQ8(int q8) { this.q8 = q8; }

    public int getQ9() { return q9; }
    public void setQ9(int q9) { this.q9 = q9; }

    public int getQ10() { return q10; }
    public void setQ10(int q10) { this.q10 = q10; }

    public int getTotalScore() {
        return q1 + q2 + q3 + q4 + q5 + q6 + q7 + q8 + q9 + q10;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            SelfEvaluation other = (SelfEvaluation) obj;
            isEqual = this.practitionerId == other.practitionerId &&
                    this.reportId == other.reportId;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(practitionerId, reportId);
    }
}