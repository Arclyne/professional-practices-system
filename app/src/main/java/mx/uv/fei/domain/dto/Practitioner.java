package mx.uv.fei.domain.dto;

import java.util.Objects;

public class Practitioner extends User {

    private String indigenousLanguage;
    private double grade;
    private String enrollment;
    private Integer groupId;

    public Practitioner() {
        super();
        this.setRole("Practitioner");
    }

    public String getIndigenousLanguage() {
        return indigenousLanguage;
    }

    public void setIndigenousLanguage(String indigenousLanguage) {
        this.indigenousLanguage = indigenousLanguage;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        this.grade = grade;
    }

    public void setEnrollment(String enrollment) {
        this.enrollment = enrollment;
    }

    public String getEnrollment() {
        return this.enrollment;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object object) {
        boolean isEqual = false;

        if (this == object) {
            isEqual = true;
        } else if (object != null && getClass() == object.getClass() && super.equals(object)) {
            Practitioner that = (Practitioner) object;
            isEqual = Objects.equals(this.enrollment, that.getEnrollment()) &&
                    Objects.equals(this.groupId, that.getGroupId());
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), enrollment, groupId);
    }
}