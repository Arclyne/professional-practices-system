package mx.uv.fei.appconfiguration;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Keep;
import mx.uv.fei.config.annotation.etiquette.Provide;
import mx.uv.fei.dataaccess.database.DatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;

import java.util.Map;

@Component
@Keep
public class DatabaseConnectionConfiguration {

    private static final String DATABASE_PROPERTIES_FILE = "database.properties";
    private static final String DATABASE_URL_KEY = "db.url";
    private static final String DATABASE_USER_KEY = "db.user";
    private static final String DATABASE_PASSWORD_KEY = "db.password";

    private final DatabaseProperties databaseProperties;
    private final String activeProfile;

    @Inject
    public DatabaseConnectionConfiguration(DatabaseProperties databaseProperties, String activeProfile) {
        this.databaseProperties = databaseProperties;
        this.activeProfile = activeProfile;
        loadDatabaseProperties();
    }

    @Provide
    public IDatabaseConnection provideConnection() {
        return new DatabaseConnection(
                databaseProperties.getUrl(),
                databaseProperties.getUser(),
                databaseProperties.getPassword()
        );
    }

    private void loadDatabaseProperties() {
        FileConfigLoader configLoader = new FileConfigLoader();
        Map<String, String> databaseConfigurationValues = configLoader.loadUseConfig(DATABASE_PROPERTIES_FILE, activeProfile);
        databaseProperties.setUrl(databaseConfigurationValues.get(DATABASE_URL_KEY));
        databaseProperties.setUser(databaseConfigurationValues.get(DATABASE_USER_KEY));
        databaseProperties.setPassword(databaseConfigurationValues.get(DATABASE_PASSWORD_KEY));
    }
}