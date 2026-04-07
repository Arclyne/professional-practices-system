package mx.uv.fei.domain.dto;

public class Practicante extends User {
    
    private String lenguaIndigena;
    private int calificacion;

    public Practicante() {
        super();
    }

    public String getLenguaIndigena() {
        return lenguaIndigena;
    }

    public void setLenguaIndigena(String lenguaIndigena) {
        this.lenguaIndigena = lenguaIndigena;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }
    
    @Override
    public String toString() {
        return "Practicante {" +
                "idUsuario=" + getId() + 
                ", nombre='" + getName() + '\'' +
                ", apellidos='" + getLastName() + '\'' +
                ", lenguaIndigena='" + lenguaIndigena + '\'' +
                ", calificacion=" + calificacion +
                '}';
    }
}