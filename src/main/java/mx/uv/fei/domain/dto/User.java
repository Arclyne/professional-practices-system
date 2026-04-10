package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String password;
    private String name;
    private String lastName;
    private String status;
    private String gender;
    private LocalDateTime registrationDate;
    private LocalDateTime unsubscriptionDate;

    public User() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getUnsubscriptionDate() {
        return unsubscriptionDate;
    }

    public void setUnsubscriptionDate(LocalDateTime unsubscriptionDate) {
        this.unsubscriptionDate = unsubscriptionDate;
    }
}