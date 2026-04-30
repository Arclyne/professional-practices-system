package mx.uv.fei.config;

import mx.uv.fei.config.annotation.etiquette.Component;

@Component
public class DatabaseProperties {

    private String Url;
    private String User;
    private String Password;

    public String getUrl() {
        return Url;
    }

    public void setUrl(String Url) {
        this.Url = Url;
    }

    public String getUser() {
        return User;
    }

    public void setUser(String User) {
        this.User = User;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String Password) {
        this.Password = Password;
    }
}
