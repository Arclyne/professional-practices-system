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

    public void setIdOrganization(int idOrganization) {
        this.idOrganization = idOrganization;
    }

    public String getNameOrganization() {
        return nameOrganization;
    }

    public void setNameOrganization(String nameOrganization) {
        this.nameOrganization = nameOrganization;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
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