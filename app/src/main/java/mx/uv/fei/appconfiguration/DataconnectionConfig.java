package mx.uv.fei.appconfiguration;

import java.util.Map;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Keep;
import mx.uv.fei.config.annotation.etiquette.Provide;
import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;

@Component
@Keep
public class DataconnectionConfig {
    private final DatabaseProperties properties;
    private final String activeProfile;

    @Inject
    public DataconnectionConfig(DatabaseProperties properties, String profile) {
        this.properties = properties;
        this.activeProfile = profile;
        loadConfig();
    }

    @Provide
    public IDatabaseConnection provideConnection() {
        return new DatabaseConnection(
                properties.getUrl(),
                properties.getUser(),
                properties.getPassword()
        );
    }

    private void loadConfig() {
        FileConfigLoader loader = new FileConfigLoader();
        var map = loader.loadUseConfig("database.properties", activeProfile);
        properties.setUrl(map.get("db.url"));
        properties.setUser(map.get("db.user"));
        properties.setPassword(map.get("db.password"));
    }
}