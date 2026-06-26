package mx.uv.fei.domain.dto;

import mx.uv.fei.domain.enums.UserStatus;

import java.util.Objects;

public class Manager {

    private int id;
    private String name;
    private String phone;
    private String email;
    private int organizationId;
    private UserStatus status;

    public Manager() {}

    public Manager(int id, String name, String phone, String email, int organizationId, UserStatus status) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.organizationId = organizationId;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getOrganizationId() { return organizationId; }
    public void setOrganizationId(int organizationId) { this.organizationId = organizationId; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Manager other = (Manager) obj;
            isEqual = Objects.equals(this.email, other.email);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }
}