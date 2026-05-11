package mx.uv.fei.domain.dto;

import java.util.Objects;

public class Practitioner extends User {

    private String indigenousLanguage;
    private double grade;
    private String enrollment;

    public Practitioner() {
        super();
    }

    public String getIndigenousLanguage() { return indigenousLanguage; }
    public void setIndigenousLanguage(String indigenousLanguage) { this.indigenousLanguage = indigenousLanguage; }

    public double getGrade() { return grade; }
    public void setGrade(double grade) { this.grade = grade; }

    public void setEnrollment(String enrollment) { this.enrollment = enrollment; }
    public String getEnrollment() { return this.enrollment; }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        if (!super.equals(object)) return false;

        Practitioner that = (Practitioner) object;
        return Objects.equals(this.enrollment, that.enrollment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enrollment);
    }
}