package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {
    private int id;
    private String password;
    private String name;
    private String lastName;
    private String status;
    private String gender;
    private String email;
    private LocalDateTime registrationDate;
    private LocalDateTime unsubscriptionDate;
    private String role;

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

    public void setEmail(String email) { this.email = email; }

    public String getEmail() { return this.email; }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LocalDateTime getUnsubscriptionDate() {
        return unsubscriptionDate;
    }

    public void setDischargeDate(LocalDateTime unsubscriptionDate) {
        this.unsubscriptionDate = unsubscriptionDate;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        User that = (User) object;

        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.lastName, that.lastName);
    }
}