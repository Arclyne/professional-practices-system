package mx.uv.fei.appconfiguration;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;

public class ApplicationConfigurationFactory {
    public static IApplicationModule create(String profile) {

        DatabaseProperties properties = new DatabaseProperties();

        DataconnectionConfig dataConfig = new DataconnectionConfig(properties);

        return new ApplicationConfiguration(dataConfig.databaseConnection(), profile);
    }
}