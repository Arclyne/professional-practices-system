package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa la calificación de un practicante asignada por un profesor en un periodo académico.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class PractitionerGrade {

    private int gradeId;
    private int practitionerId;
    private int professorId;
    private double tentativeGrade;
    private Double finalGrade;
    private String period;
    private LocalDateTime gradedAt;

    public PractitionerGrade() {}

    public int getGradeId() { return gradeId; }
    public void setGradeId(int gradeId) { this.gradeId = gradeId; }

    public int getPractitionerId() { return practitionerId; }
    public void setPractitionerId(int practitionerId) { this.practitionerId = practitionerId; }

    public int getProfessorId() { return professorId; }
    public void setProfessorId(int professorId) { this.professorId = professorId; }

    public double getTentativeGrade() { return tentativeGrade; }
    public void setTentativeGrade(double tentativeGrade) { this.tentativeGrade = tentativeGrade; }

    public Double getFinalGrade() { return finalGrade; }
    public void setFinalGrade(Double finalGrade) { this.finalGrade = finalGrade; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public LocalDateTime getGradedAt() { return gradedAt; }
    public void setGradedAt(LocalDateTime gradedAt) { this.gradedAt = gradedAt; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            PractitionerGrade other = (PractitionerGrade) obj;
            isEqual = this.practitionerId == other.practitionerId &&
                    Objects.equals(this.period, other.period);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(practitionerId, period);
    }
}