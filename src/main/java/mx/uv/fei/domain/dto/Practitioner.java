package mx.uv.fei.domain.dto;

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
}