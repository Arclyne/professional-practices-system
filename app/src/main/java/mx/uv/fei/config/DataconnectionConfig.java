package mx.uv.fei.config;

import java.util.Map;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.etiquette.Provide;
import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

@Component
@Profile("local")
public class DataconnectionConfig {

    private DatabaseProperties properties;

    @Inject
    public DataconnectionConfig(DatabaseProperties properties, String profile) {

        this.properties = properties;
        FileConfigLoader configurator = new FileConfigLoader();
        Map<String, String> propetiesLoad = configurator.loadUseConfig("database.properties", profile);

        properties.setUrl(propetiesLoad.get("db.url"));
        properties.setUser(propetiesLoad.get("db.user"));
        properties.setPassword(propetiesLoad.get("db.password"));
    }

    @Provide
    public IDatabaseConnection databaseConnection() {

        return new DatabaseConnection(properties.getUrl(), properties.getUser(), properties.getPassword());
    }
}