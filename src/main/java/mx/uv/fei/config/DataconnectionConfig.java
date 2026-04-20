package mx.uv.fei.config;

import java.util.Map;

import mx.uv.fei.dataacces.database.DatabaseConnection;

public class DataconnectionConfig {

    private DatabasePropeties propeties;

    public DataconnectionConfig(DatabasePropeties propeties, String profil) {
        this.propeties = propeties;
        FileConfigLoader configurator = new FileConfigLoader();
        Map<String, String> propetiesLoad = configurator.loadUseConfig("database.properties", profil);
        propeties.setUrl(propetiesLoad.get("db.url"));
        propeties.setUser(propetiesLoad.get("db.user"));
        propeties.setPassword(propetiesLoad.get("db.password"));
    }

    public DatabaseConnection databaseConnection() {

        return new DatabaseConnection(propeties.getUrl(), propeties.getUser(), propeties.getPassword());
    }
}