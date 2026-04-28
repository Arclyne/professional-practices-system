package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class AuthenticationToken {
    private int valueToken;
    private LocalDateTime timeCreation;
    private String userName;

    public AuthenticationToken() {
        this.timeCreation = LocalDateTime.now();
    }

    public int getValueToken() {
        return valueToken;
    }

    public void setValueToken(int value) {
        this.valueToken = value;
    }

    public LocalDateTime getTimeCreation() {
        return timeCreation;
    }

    public void setTimeCreation(LocalDateTime time) {
        this.timeCreation = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null || getClass() != object.getClass())
            return false;

        AuthenticationToken that = (AuthenticationToken) object;

        return valueToken == that.getValueToken() &&
                Objects.equals(userName, that.getUserName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(valueToken, userName);
    }
}