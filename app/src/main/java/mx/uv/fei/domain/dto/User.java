package mx.uv.fei.domain.dto;

import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Representa un usuario del sistema con sus datos de autenticación y perfil.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public class User {

    private int id;
    private String userName;
    private String password;
    private String role;
    private String name;
    private String lastName;
    private String email;
    private UserStatus status;
    private Gender gender;
    private LocalDateTime registrationDate;
    private LocalDateTime dischargeDate;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public LocalDateTime getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDateTime registrationDate) { this.registrationDate = registrationDate; }

    public LocalDateTime getDischargeDate() { return dischargeDate; }
    public void setDischargeDate(LocalDateTime dischargeDate) { this.dischargeDate = dischargeDate; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            User other = (User) obj;
            isEqual = Objects.equals(this.email, other.email);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}