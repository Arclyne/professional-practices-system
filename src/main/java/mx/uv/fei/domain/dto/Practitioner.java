package mx.uv.fei.domain.dto;

import java.util.Objects;

public class Practitioner extends User {
    
    private String indigenousLanguage;
    private double grade;

    public Practitioner() {
        super();
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

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
    
        if (object == null || getClass() != object.getClass()){
            return false;
        }

        Practitioner that = (Practitioner) object;

        return Objects.equals(this.getName(), that.getName()) &&
               Objects.equals(this.getLastName(), that.getLastName()) &&
               Objects.equals(this.indigenousLanguage, that.indigenousLanguage) &&
               Double.compare(this.grade, that.grade) == 0;
    }
}