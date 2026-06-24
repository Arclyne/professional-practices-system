package mx.uv.fei.domain.dto;

import java.util.Objects;

/**
 * Representa un estudiante inscrito en el programa de prácticas profesionales.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class Practitioner extends User {

    private String indigenousLanguage;
    private double grade;
    private String enrollment;
    private Integer groupId;

    public Practitioner() {
        super();
        this.setRole("Practitioner");
    }

    public String getIndigenousLanguage() { return indigenousLanguage; }
    public void setIndigenousLanguage(String indigenousLanguage) { this.indigenousLanguage = indigenousLanguage; }

    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    public String getEnrollment() { return enrollment; }
    public void setEnrollment(String enrollment) { this.enrollment = enrollment; }

    public Integer getGroupId() { return groupId; }
    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Practitioner other = (Practitioner) obj;
            isEqual = Objects.equals(this.enrollment, other.enrollment);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollment);
    }
}