package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;


public class AuthenticationToken {

    private int valueToken;
    private LocalDateTime timeCreation;
    private String userName;

    public AuthenticationToken() {
        this.timeCreation = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public int getValueToken() {
        return valueToken;
    }

    public void setValueToken(int valueToken) {
        this.valueToken = valueToken;
    }

    public LocalDateTime getTimeCreation() {
        return timeCreation;
    }

    public void setTimeCreation(LocalDateTime timeCreation) {
        this.timeCreation = timeCreation != null ? timeCreation.truncatedTo(ChronoUnit.SECONDS) : null;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            AuthenticationToken other = (AuthenticationToken) obj;
            isEqual = this.valueToken == other.getValueToken() &&
                    Objects.equals(this.userName, other.getUserName());
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueToken, userName);
    }
}