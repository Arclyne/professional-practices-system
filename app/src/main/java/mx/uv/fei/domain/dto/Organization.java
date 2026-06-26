package mx.uv.fei.domain.dto;

import java.util.Objects;


public class Organization {

    private int idOrganization;
    private String nameOrganization;
    private String state;
    private String adress;
    private String city;
    private String business;
    private String mail;
    private String cellphone;

    public int getIdOrganization() { return idOrganization; }
    public void setIdOrganization(int idOrganization) { this.idOrganization = idOrganization; }

    public String getNameOrganization() { return nameOrganization; }
    public void setNameOrganization(String nameOrganization) { this.nameOrganization = nameOrganization; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getBusiness() { return business; }
    public void setBusiness(String business) { this.business = business; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public String getCellphone() { return cellphone; }
    public void setCellphone(String cellphone) { this.cellphone = cellphone; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Organization other = (Organization) obj;
            isEqual = Objects.equals(this.nameOrganization, other.nameOrganization) &&
                    Objects.equals(this.mail, other.mail);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(nameOrganization, mail);
    }
}