package mx.uv.fei.domain.dto;

import java.util.Objects;

public class Organization {
    private int idOrganization;
    private String nameOrganization;
    private String region;
    private String adress;
    private String city;
    private String business;
    private String mail;
    private String cellphone;

    public int getIdOrganization() {
        return idOrganization;
    }

    public void setIdOrganization(int idEmpresa) {
        this.idOrganization = idEmpresa;
    }

    public String getNameOrganization() {
        return nameOrganization;
    }

    public void setNameOrganization(String nombreEmpresa) {
        this.nameOrganization = nombreEmpresa;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String estado) {
        this.region = estado;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String direccion) {
        this.adress = direccion;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String ciudad) {
        this.city = ciudad;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String sector) {
        this.business = sector;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String correo) {
        this.mail = correo;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String telefono) {
        this.cellphone = telefono;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Organization that = (Organization) o;

        return idOrganization == that.getIdOrganization() &&
                Objects.equals(nameOrganization, that.getNameOrganization()) &&
                Objects.equals(region, that.getRegion()) &&
                Objects.equals(mail, that.getMail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(idOrganization, nameOrganization, region, mail);
    }
}