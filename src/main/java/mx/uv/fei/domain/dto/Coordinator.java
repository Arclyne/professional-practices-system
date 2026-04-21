package mx.uv.fei.domain.dto;


import java.time.LocalDateTime;
import java.util.Objects;


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

     @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
    
        if (object == null || getClass() != object.getClass()){
            return false;
        }

        Coordinator that = (Coordinator) object;

        return Objects.equals(this.getName(), that.getName()) &&
                Objects.equals(this.getLastName(), that.getLastName());
    }
}