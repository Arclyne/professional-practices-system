package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class PractitionerGrade {

    private int gradeId;
    private int practitionerId;
    private int professorId;
    private double tentativeGrade;
    private Double finalGrade;
    private String period;
    private LocalDateTime gradedAt;

    public PractitionerGrade() {
    }

    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public int getPractitionerId() {
        return practitionerId;
    }

    public void setPractitionerId(int practitionerId) {
        this.practitionerId = practitionerId;
    }

    public int getProfessorId() {
        return professorId;
    }

    public void setProfessorId(int professorId) {
        this.professorId = professorId;
    }

    public double getTentativeGrade() {
        return tentativeGrade;
    }

    public void setTentativeGrade(double tentativeGrade) {
        this.tentativeGrade = tentativeGrade;
    }

    public Double getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(Double finalGrade) {
        this.finalGrade = finalGrade;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public LocalDateTime getGradedAt() {
        return gradedAt;
    }

    public void setGradedAt(LocalDateTime gradedAt) {
        this.gradedAt = gradedAt;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass()) {
            PractitionerGrade that = (PractitionerGrade) object;
            isEqual = this.practitionerId == that.practitionerId
                    && Objects.equals(this.period, that.period);
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(practitionerId, period);
    }
}
