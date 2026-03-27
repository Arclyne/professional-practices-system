package mx.uv.fei.domain.dto;


import java.time.LocalDateTime;


public class Coordinator extends User {
    private LocalDateTime registrationDate;
    private LocalDateTime dischargeDate;

    public Coordinator() {
        super();
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDateTime dischargeDate) {
        this.dischargeDate = dischargeDate;
    }
}