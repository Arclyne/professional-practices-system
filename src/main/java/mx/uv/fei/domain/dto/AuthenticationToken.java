package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;

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

}
