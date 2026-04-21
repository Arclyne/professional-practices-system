package mx.uv.fei.config;

import java.util.Map;
import mx.uv.fei.dataacces.database.DatabaseConnection;

public class DataconnectionConfig {

    private DatabasePropeties properties;

    public DataconnectionConfig(DatabasePropeties properties, String profile) {

        this.properties = properties;
        FileConfigLoader configurator = new FileConfigLoader();
        Map<String, String> propetiesLoad = configurator.loadUseConfig("database.properties", profile);

        properties.setUrl(propetiesLoad.get("db.url"));
        properties.setUser(propetiesLoad.get("db.user"));
        properties.setPassword(propetiesLoad.get("db.password"));
    }

    public DatabaseConnection databaseConnection() {

        return new DatabaseConnection(properties.getUrl(), properties.getUser(), properties.getPassword());
    }
}