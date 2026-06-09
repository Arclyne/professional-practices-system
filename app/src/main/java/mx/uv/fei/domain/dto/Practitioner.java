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

    public String getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(String enrollment) {
        this.enrollment = enrollment;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;

        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            isEqual = super.equals(obj);
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }
}
