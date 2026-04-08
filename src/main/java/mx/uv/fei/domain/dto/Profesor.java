package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;

public class Profesor extends User {
    
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaBaja;

    public Profesor() {
        super();
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(LocalDateTime fechaBaja) {
        this.fechaBaja = fechaBaja;
    }
}