package mx.uv.fei.domain.dto;

public class Manager {

    private int id;
    private String name;
    private String phone;
    private String email;
    private int organizationId;

    public Manager() {
    }

    public Manager(int id, String name, String phone, String email, int organizationId) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.organizationId = organizationId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }
}