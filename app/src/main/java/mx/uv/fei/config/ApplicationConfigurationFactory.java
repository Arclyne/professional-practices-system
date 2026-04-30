package mx.uv.fei.config.annotation.core;

import mx.uv.fei.config.annotation.Interfaces.IApplicationModule;
import mx.uv.fei.config.DataconnectionConfig;
import mx.uv.fei.config.DatabaseProperties;
import mx.uv.fei.config.annotation.ApplicationConfiguration;

public class ApplicationConfigurationFactory {
    public static IApplicationModule create(String profile) {

        DatabaseProperties properties = new DatabaseProperties();

        DataconnectionConfig dataConfig = new DataconnectionConfig(properties, profile);

        return new ApplicationConfiguration(dataConfig, profile);
    }
}