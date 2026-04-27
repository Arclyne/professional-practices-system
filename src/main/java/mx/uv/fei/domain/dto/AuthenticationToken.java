package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class AuthenticationToken {
    private int valueToken;
    private LocalDateTime timeCreation;
    private int UserID;

    public AuthenticationToken() {
        this.timeCreation = LocalDateTime.now();
    }

    public int getValueToken() {
        return valueToken;
    }

    public void setTimeCreation(LocalDateTime time) {
        this.timeCreation = time;
    }

    public LocalDateTime getTimeCreation() {
        return timeCreation;
    }

    public void setValueToken(int value) {
        this.valueToken = value;
    }

    public void setUserId(int UserID) {
        this.UserID = UserID;
    }

    public int getUserId() {
        return this.UserID;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;

        if (object == null || getClass() != object.getClass())
            return false;

        AuthenticationToken that = (AuthenticationToken) object;

        return valueToken == that.getValueToken() &&
                UserID == that.getUserId();
    }

    @Override
    public int hashCode() {

        return Objects.hash(valueToken, UserID);
    }
}
